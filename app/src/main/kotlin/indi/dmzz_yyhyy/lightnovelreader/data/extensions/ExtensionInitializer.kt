package indi.dmzz_yyhyy.lightnovelreader.data.extensions

import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExtensionInitializer @Inject constructor(
    private val extensionLoader: ExtensionLoader,
    private val extensionManager: ExtensionManager
) {

    /**
     * Initialize all installed extensions by loading them into the ExtensionManager
     */
    suspend fun initializeExtensions() {
        try {
            // For now, we'll create some example extensions for testing
            // In a real implementation, you would load the actual installed extensions
            
            // Create example extensions for testing
            val exampleExtensions = listOf(
                ExampleExtension(),
                SecondExampleExtension(),
                // Add more example extensions as needed
            )
            
            // Register all extensions
            exampleExtensions.forEach { extension ->
                extensionManager.registerExtension(extension)
            }
            
            // TODO: In the future, load actual installed extensions from the database
            // val installedExtensions = repositoryService.getAllInstalledExtensions().first()
            // installedExtensions.forEach { installedExtension ->
            //     if (installedExtension.isEnabled) {
            //         val extension = extensionLoader.loadExtension(installedExtension)
            //         if (extension != null) {
            //             extensionManager.registerExtension(extension)
            //         }
            //     }
            // }
        } catch (e: Exception) {
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
