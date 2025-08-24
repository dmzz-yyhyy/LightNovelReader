package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import android.content.Context
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.jar.JarExtensionLoader
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.lua.LuaExtensionParser
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionLoader @Inject constructor(
    private val context: Context,
    private val luaExtensionParser: LuaExtensionParser,
    private val jarExtensionLoader: JarExtensionLoader
) {

    private val extensionsDir = File(context.filesDir, "extensions")

    init {
        if (!extensionsDir.exists()) {
            extensionsDir.mkdirs()
        }
    }

    suspend fun loadExtension(extensionEntity: InstalledExtensionEntity): Extension? =
        withContext(Dispatchers.IO) {
            try {
                val extensionFile = getExtensionFile(extensionEntity)
                if (!extensionFile.exists()) {
                    return@withContext null
                }

                // Determine extension type and load accordingly
                when (extensionEntity.type.lowercase()) {
                    "lua" -> loadLuaExtension(extensionEntity, extensionFile)
                    "jar" -> loadJarExtension(extensionEntity, extensionFile)
                    else -> {
                        // Fallback to example extensions for development
                        when (extensionEntity.id) {
                            1 -> ExampleExtension()
                            2 -> SecondExampleExtension()
                            else -> null
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    private suspend fun loadLuaExtension(extensionEntity: InstalledExtensionEntity, extensionFile: File): Extension? {
        return try {
            // Validate the Lua script first
            val luaScript = extensionFile.readText()
            when (val validationResult = luaExtensionParser.validateLuaExtension(luaScript)) {
                is LuaExtensionParser.ValidationResult.Success -> {
                    luaExtensionParser.parseExtension(extensionFile, extensionEntity)
                }
                is LuaExtensionParser.ValidationResult.MissingFunctions -> {
                    println("Lua extension validation failed. Missing functions: ${validationResult.functions}")
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun loadJarExtension(extensionEntity: InstalledExtensionEntity, extensionFile: File): Extension? {
        return try {
            // Validate the JAR file first
            when (val validationResult = jarExtensionLoader.validateJarExtension(extensionFile)) {
                is JarExtensionLoader.ValidationResult.Success -> {
                    jarExtensionLoader.loadExtension(extensionFile, extensionEntity)
                }
                is JarExtensionLoader.ValidationResult.NoExtensionClass -> {
                    println("JAR extension validation failed: No extension class found")
                    null
                }
                is JarExtensionLoader.ValidationResult.InvalidJar -> {
                    println("JAR extension validation failed: ${validationResult.error}")
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveExtension(extensionEntity: InstalledExtensionEntity, extensionData: ByteArray) =
        withContext(Dispatchers.IO) {
            try {
                val extensionFile = getExtensionFile(extensionEntity)
                extensionFile.writeBytes(extensionData)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }

    suspend fun deleteExtension(extensionEntity: InstalledExtensionEntity) =
        withContext(Dispatchers.IO) {
            try {
                val extensionFile = getExtensionFile(extensionEntity)
                if (extensionFile.exists()) {
                    extensionFile.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }

    fun getExtensionFile(extensionEntity: InstalledExtensionEntity): File {
        val extension = when (extensionEntity.type.lowercase()) {
            "lua" -> "lua"
            "jar" -> "jar"
            else -> "lua" // Default to lua
        }
        return File(extensionsDir, "${extensionEntity.id}.${extension}")
    }
}
