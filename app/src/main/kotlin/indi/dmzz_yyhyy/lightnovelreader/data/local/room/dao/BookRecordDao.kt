package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookDate
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import io.nightfish.lightnovelreader.api.identifier.Identifier
import java.time.LocalDate

@Dao
@TypeConverters(
    LocalDateTimeConverter::class
)
interface BookRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookRecord(record: BookRecordEntity)

    @Query("SELECT * FROM book_records WHERE book_id GLOB 'book:' || :namespace || ':*'")
    suspend fun getAllBookRecords(namespace: String): List<BookRecordEntity>

    @Query("SELECT * FROM book_records WHERE date = :date AND book_id GLOB 'book:' || :namespace || ':*'")
    suspend fun getBookRecordsForDate(date: LocalDate, namespace: String): List<BookRecordEntity>

    @Query("SELECT * FROM book_records WHERE date BETWEEN :start AND :end AND book_id GLOB 'book:' || :namespace || ':*'")
    suspend fun getBookRecordsBetweenDates(
        start: LocalDate,
        end: LocalDate,
        namespace: String
    ): List<BookRecordEntity>

    @Query("SELECT * FROM book_records WHERE book_id = :bookId AND date = :date")
    suspend fun getBookRecordByIdAndDate(
        bookId: Identifier.Book,
        date: LocalDate
    ): BookRecordEntity?

    @Query("SELECT * FROM book_records WHERE book_id = :bookId")
    suspend fun getBookRecordsByBookId(bookId: Identifier.Book): List<BookRecordEntity>

    @Query("SELECT MIN(date) FROM book_records WHERE book_id = :bookId AND reads > 0")
    suspend fun getFirstReadDate(bookId: Identifier.Book): LocalDate?

    @Query("SELECT MIN(date) FROM book_records WHERE book_id = :bookId AND is_finished = 1")
    suspend fun getFirstFinishedDate(bookId: Identifier.Book): LocalDate?

    @Query("SELECT book_id, MIN(date) AS date FROM book_records WHERE reads > 0 AND book_id GLOB 'book:' || :namespace || ':*' GROUP BY book_id")
    suspend fun getFirstReadDates(namespace: String): List<BookDate>

    @Query("SELECT book_id, MIN(date) AS date FROM book_records WHERE is_finished = 1 AND book_id GLOB 'book:' || :namespace || ':*' GROUP BY book_id")
    suspend fun getFirstFinishedDates(namespace: String): List<BookDate>

    @Query("SELECT book_id, MIN(date) AS date FROM book_records WHERE is_favorited = 1 AND book_id GLOB 'book:' || :namespace || ':*' GROUP BY book_id")
    suspend fun getFirstFavoritedDates(namespace: String): List<BookDate>

    @Query("DELETE FROM book_records WHERE book_id GLOB 'book:' || :namespace || ':*'")
    suspend fun clearRecords(namespace: String)

    @Transaction
    suspend fun clear(namespace: String) {
        clearRecords(namespace)
    }
}

