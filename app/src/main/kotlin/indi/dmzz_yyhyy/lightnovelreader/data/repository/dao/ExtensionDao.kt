package indi.dmzz_yyhyy.lightnovelreader.data.repository.dao

import androidx.room.*
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtensionDao {
    @Query("SELECT * FROM repository_extensions")
    fun getAllExtensions(): Flow<List<ExtensionEntity>>

    @Query("SELECT * FROM repository_extensions WHERE repoId = :repoId")
    fun getExtensionsByRepoId(repoId: Int): Flow<List<ExtensionEntity>>

    @Query("SELECT * FROM repository_extensions WHERE id = :id AND repoId = :repoId")
    suspend fun getExtensionById(repoId: Int, id: Int): ExtensionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtension(extension: ExtensionEntity): Long

    @Update
    suspend fun updateExtension(extension: ExtensionEntity)

    @Delete
    suspend fun deleteExtension(extension: ExtensionEntity)

    @Query("DELETE FROM repository_extensions WHERE repoId = :repoId")
    suspend fun deleteExtensionsByRepoId(repoId: Int)

    @Query("DELETE FROM repository_extensions WHERE id = :id AND repoId = :repoId")
    suspend fun deleteExtensionById(repoId: Int, id: Int)
}
