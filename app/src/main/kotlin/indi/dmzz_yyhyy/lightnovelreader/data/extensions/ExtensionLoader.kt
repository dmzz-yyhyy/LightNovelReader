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

    suspend fun loadExtension(extensionEntity: InstalledExtensionEntity): Extension? = withContext(Dispatchers.IO) {
        try {
            val extensionFile = File(extensionsDir, "${extensionEntity.id}.jar")
            if (!extensionFile.exists()) {
                return@withContext null
            }

            // For now, return the example extension as a placeholder
            // In a real implementation, you would load the JAR file and instantiate the extension
            when (extensionEntity.id) {
                1 -> ExampleExtension()
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveExtension(extensionEntity: InstalledExtensionEntity, extensionData: ByteArray) = withContext(Dispatchers.IO) {
        try {
            val extensionFile = File(extensionsDir, "${extensionEntity.id}.jar")
            extensionFile.writeBytes(extensionData)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun deleteExtension(extensionEntity: InstalledExtensionEntity) = withContext(Dispatchers.IO) {
        try {
            val extensionFile = File(extensionsDir, "${extensionEntity.id}.jar")
            if (extensionFile.exists()) {
                extensionFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    fun getExtensionFile(extensionEntity: InstalledExtensionEntity): File {
        return File(extensionsDir, "${extensionEntity.id}.jar")
    }
}
