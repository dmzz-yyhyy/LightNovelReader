package indi.dmzz_yyhyy.lightnovelreader.data.extensions.update

import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryService
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages extension updates
 */
@Singleton
class ExtensionUpdateManager @Inject constructor(
    private val repositoryService: RepositoryService
) {

    /**
     * Check for available updates for all installed extensions
     */
    suspend fun checkForUpdates(): List<ExtensionUpdate> {
        val installedExtensions = repositoryService.getAllInstalledExtensions().first()
        val availableExtensions = repositoryService.getAllExtensions().first()
        
        val updates = mutableListOf<ExtensionUpdate>()
        
        installedExtensions.forEach { installed ->
            val available = availableExtensions.find { 
                it.id == installed.id && it.repoId == installed.repoId 
            }
            
            if (available != null && isUpdateAvailable(installed, available)) {
                updates.add(
                    ExtensionUpdate(
                        installedExtension = installed,
                        availableExtension = available,
                        currentVersion = installed.version,
                        newVersion = available.version
                    )
                )
            }
        }
        
        return updates
    }

    /**
     * Update a specific extension
     */
    suspend fun updateExtension(update: ExtensionUpdate): UpdateResult {
        return try {
            // Uninstall current version
            repositoryService.uninstallExtension(update.installedExtension)
            
            // Install new version
            repositoryService.installExtension(update.availableExtension)
            
            UpdateResult.Success
        } catch (e: Exception) {
            UpdateResult.Error(e.message ?: "Update failed")
        }
    }

    /**
     * Update multiple extensions
     */
    suspend fun updateExtensions(updates: List<ExtensionUpdate>): Map<ExtensionUpdate, UpdateResult> {
        val results = mutableMapOf<ExtensionUpdate, UpdateResult>()
        
        updates.forEach { update ->
            results[update] = updateExtension(update)
        }
        
        return results
    }

    /**
     * Check if an update is available for a specific extension
     */
    private fun isUpdateAvailable(installed: InstalledExtensionEntity, available: ExtensionEntity): Boolean {
        return compareVersions(available.version, installed.version) > 0
    }

    /**
     * Compare two version strings
     */
    private fun compareVersions(version1: String, version2: String): Int {
        val v1Parts = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = version2.split(".").map { it.toIntOrNull() ?: 0 }
        
        val maxLength = maxOf(v1Parts.size, v2Parts.size)
        
        for (i in 0 until maxLength) {
            val v1Part = v1Parts.getOrNull(i) ?: 0
            val v2Part = v2Parts.getOrNull(i) ?: 0
            
            when {
                v1Part > v2Part -> return 1
                v1Part < v2Part -> return -1
            }
        }
        
        return 0
    }
}

data class ExtensionUpdate(
    val installedExtension: InstalledExtensionEntity,
    val availableExtension: ExtensionEntity,
    val currentVersion: String,
    val newVersion: String
)

sealed class UpdateResult {
    object Success : UpdateResult()
    data class Error(val message: String) : UpdateResult()
}
