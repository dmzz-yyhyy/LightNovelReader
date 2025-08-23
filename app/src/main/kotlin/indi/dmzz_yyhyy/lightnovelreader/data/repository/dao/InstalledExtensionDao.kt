package indi.dmzz_yyhyy.lightnovelreader.data.repository.dao

import androidx.room.*
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalledExtensionDao {
    @Query("SELECT * FROM installed_extensions")
    fun getAllInstalledExtensions(): Flow<List<InstalledExtensionEntity>>

    @Query("SELECT * FROM installed_extensions WHERE id = :id")
    fun getInstalledExtensionById(id: Int): Flow<InstalledExtensionEntity?>

    @Query("SELECT * FROM installed_extensions WHERE id = :id")
    suspend fun getInstalledExtensionByIdSync(id: Int): InstalledExtensionEntity?

    @Query("SELECT * FROM installed_extensions WHERE isEnabled = 1")
    fun getEnabledExtensions(): Flow<List<InstalledExtensionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstalledExtension(extension: InstalledExtensionEntity): Long

    @Update
    suspend fun updateInstalledExtension(extension: InstalledExtensionEntity)

    @Delete
    suspend fun deleteInstalledExtension(extension: InstalledExtensionEntity)

    @Query("DELETE FROM installed_extensions WHERE id = :id")
    suspend fun deleteInstalledExtensionById(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM installed_extensions WHERE id = :id)")
    suspend fun isExtensionInstalled(id: Int): Boolean
}
