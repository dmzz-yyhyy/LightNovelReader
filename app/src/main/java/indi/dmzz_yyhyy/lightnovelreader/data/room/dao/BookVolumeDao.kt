package indi.dmzz_yyhyy.lightnovelreader.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import indi.dmzz_yyhyy.lightnovelreader.data.room.`object`.BDVolume

@Dao
abstract class BookVolumeDao {
    @Transaction
    @Query("SELECT * FROM bookvolume WHERE id=:id")
    abstract suspend fun get(id: Int): List<BDVolume>
    

    /*
    @Insert
    abstract suspend fun add()
    @Query("DELETE FROM bookvolume WHERE id=:id")
    abstract suspend fun delete(id: Int)
     */
}