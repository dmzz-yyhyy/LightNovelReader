package indi.dmzz_yyhyy.lightnovelreader.data.repository

import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionLoader
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.RepositoryEntity
import indi.dmzz_yyhyy.lightnovelreader.data.repository.remote.RepositoryRemoteDataSource
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryService @Inject constructor(
    private val repositoryRepository: RepositoryRepository,
    private val extensionRepository: ExtensionRepository,
    private val remoteDataSource: RepositoryRemoteDataSource,
    private val extensionLoader: ExtensionLoader
) {

    fun getAllRepositories(): Flow<List<RepositoryEntity>> =
        repositoryRepository.getAllRepositories()

    fun getEnabledRepositories(): Flow<List<RepositoryEntity>> =
        repositoryRepository.getEnabledRepositories()

    suspend fun addRepository(name: String, url: String): Long {
        return repositoryRepository.addRepository(name, url)
    }

    suspend fun updateRepository(repository: RepositoryEntity) {
        repositoryRepository.updateRepository(repository)
    }

    suspend fun deleteRepository(repository: RepositoryEntity) {
        // Delete all extensions from this repository first
        extensionRepository.deleteExtensionsByRepoId(repository.id)
        // Then delete the repository
        repositoryRepository.deleteRepository(repository)
    }

    suspend fun refreshRepository(repository: RepositoryEntity) {
        try {
            val repoData = remoteDataSource.downloadRepoData(repository)
            
            // Update repository with new data
            val updatedRepository = repository.copy(
                name = repoData.name,
                lastUpdated = System.currentTimeMillis()
            )
            repositoryRepository.updateRepository(updatedRepository)

            // Clear existing extensions and add new ones
            extensionRepository.deleteExtensionsByRepoId(repository.id)
            
            repoData.extensions.forEach { repoExtension ->
                val extension = ExtensionEntity(
                    id = repoExtension.id,
                    repoId = repository.id,
                    name = repoExtension.name,
                    fileName = repoExtension.fileName,
                    imageURL = repoExtension.imageURL,
                    lang = repoExtension.lang,
                    version = repoExtension.version,
                    md5 = repoExtension.md5,
                    type = repoExtension.type,
                    description = repoExtension.description
                )
                extensionRepository.insertExtension(extension)
            }
        } catch (e: IOException) {
            throw e
        }
    }

    fun getAllExtensions(): Flow<List<ExtensionEntity>> =
        extensionRepository.getAllExtensions()

    fun getExtensionsByRepoId(repoId: Int): Flow<List<ExtensionEntity>> =
        extensionRepository.getExtensionsByRepoId(repoId)

    suspend fun installExtension(extension: ExtensionEntity): ByteArray {
        val repository = repositoryRepository.getRepositoryById(extension.repoId)
            ?: throw IOException("Repository not found")

        val extensionData = remoteDataSource.downloadExtension(repository, extension.fileName)
        
        val installedExtension = InstalledExtensionEntity(
            id = extension.id,
            repoId = extension.repoId,
            name = extension.name,
            fileName = extension.fileName,
            imageURL = extension.imageURL,
            lang = extension.lang,
            version = extension.version,
            md5 = extension.md5,
            type = extension.type,
            description = extension.description,
            isEnabled = true,
            installDate = System.currentTimeMillis()
        )
        
        // Save the extension file
        extensionLoader.saveExtension(installedExtension, extensionData)
        
        // Insert into database
        extensionRepository.insertInstalledExtension(installedExtension)
        return extensionData
    }

    suspend fun uninstallExtension(extension: InstalledExtensionEntity) {
        // Delete the extension file
        extensionLoader.deleteExtension(extension)
        
        // Delete from database
        extensionRepository.deleteInstalledExtension(extension)
    }

    suspend fun updateInstalledExtension(extension: InstalledExtensionEntity) {
        extensionRepository.updateInstalledExtension(extension)
    }

    fun getAllInstalledExtensions(): Flow<List<InstalledExtensionEntity>> =
        extensionRepository.getAllInstalledExtensions()

    fun getEnabledExtensions(): Flow<List<InstalledExtensionEntity>> =
        extensionRepository.getEnabledExtensions()

    suspend fun isExtensionInstalled(id: Int): Boolean =
        extensionRepository.isExtensionInstalled(id)
}
