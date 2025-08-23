package indi.dmzz_yyhyy.lightnovelreader.data.repository.dao

import androidx.room.*
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.RepositoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepositoryDao {
    @Query("SELECT * FROM repositories")
    fun getAllRepositories(): Flow<List<RepositoryEntity>>

    @Query("SELECT * FROM repositories WHERE isEnabled = 1")
    fun getEnabledRepositories(): Flow<List<RepositoryEntity>>

    @Query("SELECT * FROM repositories WHERE id = :id")
    suspend fun getRepositoryById(id: Int): RepositoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepository(repository: RepositoryEntity): Long

    @Update
    suspend fun updateRepository(repository: RepositoryEntity)

    @Delete
    suspend fun deleteRepository(repository: RepositoryEntity)

    @Query("DELETE FROM repositories WHERE id = :id")
    suspend fun deleteRepositoryById(id: Int)
}
