package indi.dmzz_yyhyy.lightnovelreader.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.BookMetadata

@Dao
abstract class BookMetadataDao {
    @Query("SELECT * FROM bookmetadata WHERE id LiKE :id LIMIT 1")
    abstract suspend fun get(id: Int): BookMetadata?

    @Query("SELECT * FROM bookmetadata WHERE id IN (:idList)")
    abstract suspend fun getByIdList(idList: List<Int>): List<BookMetadata>?

    @Query("SELECT lastReadChapterId FROM bookmetadata WHERE id = :bookId")
    abstract suspend fun getBookLastReadChapterId(bookId: Int): Int
    @Query("UPDATE bookmetadata SET lastReadChapterId = :lastReadChapterId WHERE id = :bookId")
    abstract suspend fun setBookLastReadChapterId(bookId: Int, lastReadChapterId: Int)

    @Insert
    abstract suspend fun add(book: BookMetadata)

    @Query("DELETE FROM bookmetadata WHERE id=:id")
    abstract suspend fun delete(id: Int)
}