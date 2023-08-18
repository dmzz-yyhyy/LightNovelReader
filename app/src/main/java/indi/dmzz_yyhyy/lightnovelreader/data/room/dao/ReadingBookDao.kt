package indi.dmzz_yyhyy.lightnovelreader.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.ReadingBook
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ReadingBookDao {
    @Query("SELECT * FROM readingbook")
    abstract fun getAll(): Flow<List<ReadingBook>>
    @Insert
    abstract suspend fun add(book: ReadingBook)
    @Query("DELETE FROM readingbook WHERE bookId=:bookId")
    abstract suspend fun delete(bookId: Int)
}