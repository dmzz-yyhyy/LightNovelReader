package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionInitializer @Inject constructor(
    private val extensionLoader: ExtensionLoader,
    private val extensionManager: ExtensionManager,
    private val repositoryServiceProvider: javax.inject.Provider<indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryService>
) {

    /**
     * Initialize all installed extensions by loading them into the ExtensionManager
     */
    suspend fun initializeExtensions() {
        try {
            println("ExtensionInitializer: Starting extension initialization...")
            
            val installedExtensions = repositoryServiceProvider.get().getAllInstalledExtensions().first()
            println("ExtensionInitializer: Found ${installedExtensions.size} installed extensions")
            
            installedExtensions.forEach { installedExtension ->
                println("ExtensionInitializer: Processing extension: ${installedExtension.name} (ID: ${installedExtension.id}, Enabled: ${installedExtension.isEnabled})")
                
                if (installedExtension.isEnabled) {
                    val extension = extensionLoader.loadExtension(installedExtension)
                    if (extension != null) {
                        extensionManager.registerExtension(extension)
                        println("ExtensionInitializer: Successfully loaded and registered extension: ${extension.name}")
                    } else {
                        println("ExtensionInitializer: Failed to load extension: ${installedExtension.name}")
                    }
                } else {
                    println("ExtensionInitializer: Extension ${installedExtension.name} is disabled, skipping")
                }
            }
            
            println("ExtensionInitializer: Extension initialization completed. Total registered: ${extensionManager.getAllExtensions().size}")
        } catch (e: Exception) {
            println("ExtensionInitializer: Error during initialization: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Load a specific extension by its entity
     */
    suspend fun loadExtension(installedExtension: indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity): Boolean {
        return try {
            val extension = extensionLoader.loadExtension(installedExtension)
            if (extension != null) {
                extensionManager.registerExtension(extension)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Unload a specific extension
     */
    fun unloadExtension(extensionId: String) {
    extensionManager.unregisterExtension(extensionId)
    }
}
