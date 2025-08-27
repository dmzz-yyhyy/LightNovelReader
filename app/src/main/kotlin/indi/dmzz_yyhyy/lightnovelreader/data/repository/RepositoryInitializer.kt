package indi.dmzz_yyhyy.lightnovelreader.data.repository

import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.RepositoryEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryInitializer @Inject constructor(
    private val repositoryService: RepositoryService
) {

    /**
     * Initialize repositories on app startup
     */
    suspend fun initializeRepositories() {
        if (!areDefaultRepositoriesInitialized()) {
            initializeDefaultRepositories()
        }
        
        // For development: add sample extensions if no extensions are installed
        initializeSampleExtensions()
    }

    /**
     * Initialize default repositories from Shosetsu
     * These are the same repositories that Shosetsu uses by default
     */
    suspend fun initializeDefaultRepositories() {
        val existingRepositories = repositoryService.getAllRepositories().first()
        
        // Shosetsu's Main repository (curated extensions)
        val mainRepo = RepositoryEntity(
            name = "Shosetsu Main",
            url = "https://gitlab.com/shosetsuorg/extensions-main/-/raw/main/",
            isEnabled = true,
            lastUpdated = System.currentTimeMillis()
        )
        
        // Shosetsu's Universe repository (community extensions)
        val universeRepo = RepositoryEntity(
            name = "Shosetsu Universe", 
            url = "https://gitlab.com/shosetsuorg/extensions/-/raw/dev/",
            isEnabled = true,
            lastUpdated = System.currentTimeMillis()
        )

        // Add repositories if they don't already exist
        if (!existingRepositories.any { it.url == mainRepo.url }) {
            repositoryService.addRepository(mainRepo.name, mainRepo.url)
        }
        
        if (!existingRepositories.any { it.url == universeRepo.url }) {
            repositoryService.addRepository(universeRepo.name, universeRepo.url)
        }
    }

    /**
     * Check if default repositories are initialized
     */
    suspend fun areDefaultRepositoriesInitialized(): Boolean {
        val existingRepositories = repositoryService.getAllRepositories().first()
        val mainRepoUrl = "https://gitlab.com/shosetsuorg/extensions-main/-/raw/main/"
        val universeRepoUrl = "https://gitlab.com/shosetsuorg/extensions/-/raw/dev/"
        
        return existingRepositories.any { it.url == mainRepoUrl } &&
               existingRepositories.any { it.url == universeRepoUrl }
    }

    /**
     * Initialize sample extensions for development and testing
     */
    private suspend fun initializeSampleExtensions() {
        val installedExtensions = repositoryService.getAllInstalledExtensions().first()
        
        // Only add sample extensions if none are installed
        if (installedExtensions.isEmpty()) {
            val sampleExtension1 = indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity(
                id = 1,
                repoId = 0, // No repository for sample extensions
                name = "Example Extension",
                fileName = "example_extension",
                imageURL = "",
                lang = "en",
                version = "1.0.0",
                md5 = "",
                type = "example", // Special type for example extensions
                description = "An example extension for demonstration",
                isEnabled = true,
                installDate = System.currentTimeMillis()
            )
            
            val sampleExtension2 = indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity(
                id = 2,
                repoId = 0, // No repository for sample extensions
                name = "Second Example Extension", 
                fileName = "second_example_extension",
                imageURL = "",
                lang = "en",
                version = "1.0.0",
                md5 = "",
                type = "example", // Special type for example extensions
                description = "A second example extension for demonstration",
                isEnabled = true,
                installDate = System.currentTimeMillis()
            )
            
            try {
                repositoryService.insertInstalledExtension(sampleExtension1)
                repositoryService.insertInstalledExtension(sampleExtension2)
                println("RepositoryInitializer: Added sample extensions for development")
            } catch (e: Exception) {
                println("RepositoryInitializer: Failed to add sample extensions: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
