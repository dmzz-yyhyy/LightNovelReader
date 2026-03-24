package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import java.time.LocalDate

@Dao
@TypeConverters(
    LocalDateTimeConverter::class
)
interface BookRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBookRecord(record: BookRecordEntity)

    @Query("SELECT * FROM book_records")
    fun getAllBookRecords(): List<BookRecordEntity>

    @Query("SELECT * FROM book_records WHERE date = :date")
    suspend fun getBookRecordsForDate(date: LocalDate): List<BookRecordEntity>

    @Query("SELECT * FROM book_records WHERE date BETWEEN :start AND :end")
    suspend fun getBookRecordsBetweenDates(start: LocalDate, end: LocalDate): List<BookRecordEntity>

    @Query("SELECT * FROM book_records WHERE book_id = :bookId AND date = :date")
    suspend fun getBookRecordByIdAndDate(bookId: String, date: LocalDate): BookRecordEntity?

    @Query("SELECT * FROM book_records WHERE book_id = :bookId")
    suspend fun getBookRecordsByBookId(bookId: String): List<BookRecordEntity>

    @Query("SELECT MIN(date) FROM book_records WHERE book_id = :bookId AND reads > 0")
    suspend fun getFirstReadDate(bookId: String): LocalDate?

    @Query("SELECT MIN(date) FROM book_records WHERE book_id = :bookId AND is_finished = 1")
    suspend fun getFirstFinishedDate(bookId: String): LocalDate?

    @Query("SELECT book_id, MIN(date) AS date FROM book_records WHERE reads > 0 GROUP BY book_id")
    suspend fun getFirstReadDates(): List<BookDate>

    @Query("SELECT book_id, MIN(date) AS date FROM book_records WHERE is_finished = 1 GROUP BY book_id")
    suspend fun getFirstFinishedDates(): List<BookDate>

    @Query("SELECT book_id, MIN(date) AS date FROM book_records WHERE is_favorited = 1 GROUP BY book_id")
    suspend fun getFirstFavoritedDates(): List<BookDate>

    @Query("DELETE FROM book_records")
    fun clearRecords()

    @Transaction
    fun clear() {
        clearRecords()
    }
}

data class BookDate(
    @ColumnInfo(name = "book_id")
    val bookId: String,
    @ColumnInfo(name = "date")
    val date: LocalDate
)