package indi.dmzz_yyhyy.lightnovelreader.data.repository.dao

import androidx.room.*
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtensionSettingDao {
    
    @Query("SELECT * FROM extension_settings WHERE extensionId = :extensionId")
    fun getExtensionSettings(extensionId: Int): Flow<List<ExtensionSettingEntity>>
    
    @Query("SELECT * FROM extension_settings WHERE extensionId = :extensionId AND key = :key")
    suspend fun getExtensionSetting(extensionId: Int, key: String): ExtensionSettingEntity?
    
    @Query("SELECT value FROM extension_settings WHERE extensionId = :extensionId AND key = :key")
    suspend fun getSettingValue(extensionId: Int, key: String): String?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: ExtensionSettingEntity): Long
    
    @Update
    suspend fun updateSetting(setting: ExtensionSettingEntity)
    
    @Delete
    suspend fun deleteSetting(setting: ExtensionSettingEntity)
    
    @Query("DELETE FROM extension_settings WHERE extensionId = :extensionId")
    suspend fun deleteExtensionSettings(extensionId: Int)
    
    @Query("UPDATE extension_settings SET value = :value WHERE extensionId = :extensionId AND key = :key")
    suspend fun updateSettingValue(extensionId: Int, key: String, value: String)
}
