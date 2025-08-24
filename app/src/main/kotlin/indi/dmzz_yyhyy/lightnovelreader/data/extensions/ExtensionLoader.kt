package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import android.content.Context
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionLoader @Inject constructor(
    private val context: Context
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
            // TODO: Implement Lua extension loading
            // For now, return example extensions based on ID
            when (extensionEntity.id) {
                1 -> ExampleExtension()
                2 -> SecondExampleExtension()
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun loadJarExtension(extensionEntity: InstalledExtensionEntity, extensionFile: File): Extension? {
        return try {
            // TODO: Implement JAR extension loading using ClassLoader
            // This would involve loading the JAR file and instantiating the Extension class
            null
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
