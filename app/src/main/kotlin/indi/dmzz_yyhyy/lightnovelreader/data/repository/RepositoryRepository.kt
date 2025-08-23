package indi.dmzz_yyhyy.lightnovelreader.data.repository

import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.RepositoryEntity
import kotlinx.coroutines.flow.Flow

interface RepositoryRepository {
    fun getAllRepositories(): Flow<List<RepositoryEntity>>
    fun getEnabledRepositories(): Flow<List<RepositoryEntity>>
    suspend fun getRepositoryById(id: Int): RepositoryEntity?
    suspend fun addRepository(name: String, url: String): Long
    suspend fun updateRepository(repository: RepositoryEntity)
    suspend fun deleteRepository(repository: RepositoryEntity)
    suspend fun deleteRepositoryById(id: Int)
}
