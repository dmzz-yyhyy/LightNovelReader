package indi.dmzz_yyhyy.lightnovelreader.data.json

import com.google.gson.annotations.SerializedName
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookRecordEntity
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ReadingStatisticsEntity
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import java.time.LocalDate

data class DailyReadingStats(
    @SerializedName("date")
    val date: LocalDate,
    @SerializedName("reading_time_count")
    val readingTimeCount: Count,
    @SerializedName("book_records")
    val bookRecords: List<BookRecordData>
)

data class BookRecordData(
    @SerializedName("book_id")
    val bookId: String,
    @SerializedName("date")
    val date: LocalDate,
    @SerializedName("reading_time_count")
    val readingTimeCount: Count,
    @SerializedName("read_count")
    val readCount: Int,
    @SerializedName("is_finished")
    val isFinished: Boolean = false
)

fun ReadingStatisticsEntity.toDailyStatsData(bookRecords: List<BookRecordEntity>): DailyReadingStats {
    return DailyReadingStats(
        date = this.date,
        readingTimeCount = this.readingTimeCount,
        bookRecords = bookRecords.map { it.toData() }
    )
}

fun BookRecordEntity.toData(): BookRecordData {
    return BookRecordData(
        bookId = this.bookId,
        date = this.date,
        readingTimeCount = this.readingTimeCount,
        readCount = this.readCount,
        isFinished = this.isFinished
    )
}

fun BookRecordData.toEntity(): BookRecordEntity {
    return BookRecordEntity(
        bookId = this.bookId,
        date = this.date,
        readingTimeCount = this.readingTimeCount,
        readCount = this.readCount,
        isFinished = this.isFinished
    )
}
