package indi.dmzz_yyhyy.lightnovelreader.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
abstract class BookMetadataDao {
    @Query("SELECT * FROM bookmetadata WHERE bookId=:bookId")
    abstract suspend fun get(bookId: Int): BookMetadataDao
    @Insert
    abstract suspend fun add(book: BookMetadataDao)
    @Query("DELETE FROM bookmetadata WHERE bookId=:bookId")
    abstract suspend fun delete(bookId: Int)
}