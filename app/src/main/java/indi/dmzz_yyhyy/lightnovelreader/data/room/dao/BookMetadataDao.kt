package indi.dmzz_yyhyy.lightnovelreader.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.BookMetadata
import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.ReadingBook

@Dao
abstract class BookMetadataDao {
    @Query("SELECT * FROM bookmetadata WHERE id LiKE :id LIMIT 1")
    abstract suspend fun get(id: Int): BookMetadata
    @Insert
    abstract suspend fun add(book: ReadingBook)
    @Query("DELETE FROM bookmetadata WHERE id=:id")
    abstract suspend fun delete(id: Int)
}