package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.DataSourceEntity

@Dao
interface DataSourceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun register(entity: DataSourceEntity)
    @Query("SELECT * FROM datasource")
    suspend fun getAll(): List<DataSourceEntity>
}
