package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao: io.nightfish.lightnovelreader.api.userdata.UserDataDaoApi {
    @Query("replace into user_data (path, `group`, type, value) " +
            "values (:path, :group, :type, :value)")
    override fun insert(path: String, group: String, type: String, value: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userDataEntity: UserDataEntity)

    @Query("select value from user_data where path = :path")
    override fun get(path: String): String?

    @Query("select value from user_data where path = :path")
    override fun getFlow(path: String): Flow<String?>

    @Query("select * from user_data where path = :path")
    fun getEntity(path: String): UserDataEntity?

    @Query("select * from user_data where `group` = :group")
    fun getGroupValues(group: String): List<UserDataEntity>

    @Query("delete from user_data where path = :path")
    override fun remove(path: String)

    @Query("select * from user_data")
    fun getAllEntities(): List<UserDataEntity>
}