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
}
