package indi.dmzz_yyhyy.lightnovelreader.data.repository

import indi.dmzz_yyhyy.lightnovelreader.data.repository.dao.RepositoryDao
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.RepositoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RepositoryRepositoryImpl @Inject constructor(
    private val repositoryDao: RepositoryDao
) : RepositoryRepository {

    override fun getAllRepositories(): Flow<List<RepositoryEntity>> =
        repositoryDao.getAllRepositories()

    override fun getEnabledRepositories(): Flow<List<RepositoryEntity>> =
        repositoryDao.getEnabledRepositories()

    override suspend fun getRepositoryById(id: Int): RepositoryEntity? =
        repositoryDao.getRepositoryById(id)

    override suspend fun addRepository(name: String, url: String): Long {
        val repository = RepositoryEntity(
            name = name,
            url = url,
            isEnabled = true,
            lastUpdated = System.currentTimeMillis()
        )
        return repositoryDao.insertRepository(repository)
    }

    override suspend fun updateRepository(repository: RepositoryEntity) {
        repositoryDao.updateRepository(repository)
    }

    override suspend fun deleteRepository(repository: RepositoryEntity) {
        repositoryDao.deleteRepository(repository)
    }

    override suspend fun deleteRepositoryById(id: Int) {
        repositoryDao.deleteRepositoryById(id)
    }
}
