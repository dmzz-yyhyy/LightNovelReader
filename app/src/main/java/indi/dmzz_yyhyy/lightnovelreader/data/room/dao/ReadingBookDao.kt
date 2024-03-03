package indi.dmzz_yyhyy.lightnovelreader.data.room.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
abstract class ReadingBookDao {
    @Query("SELECT * FROM readingbook")
    abstract fun getAll(): List<Int>
    @Query("INSERT INTO readingbook VALUES (:id)")
    abstract suspend fun add(id: Int)

    @Query("DELETE FROM readingbook WHERE id=:id")
    abstract suspend fun delete(id: Int)
}