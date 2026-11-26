package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserDataEntity
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.userdata.UserDataDaoApi
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao : UserDataDaoApi {
    @Query("INSERT OR REPLACE INTO user_data (id, value) VALUES (:id, :value)")
    override suspend fun insert(id: Identifier.SettingPath, value: String)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: UserDataEntity)

    @Query("SELECT value FROM user_data WHERE id = :id")
    override suspend fun get(id: Identifier.SettingPath): String?

    @Query("SELECT value FROM user_data WHERE id = :id")
    override fun getFlow(id: Identifier.SettingPath): Flow<String?>

    @Query("SELECT * FROM user_data WHERE id = :id")
    suspend fun getEntity(id: Identifier.SettingPath): UserDataEntity?

    @Query("DELETE FROM user_data WHERE id = :id")
    override suspend fun remove(id: Identifier.SettingPath)

    @Query("SELECT * FROM user_data")
    suspend fun getAllEntities(): List<UserDataEntity>
}
