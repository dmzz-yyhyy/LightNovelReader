package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.CountConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import java.time.LocalDate
import java.time.LocalTime

@Serializable
@TypeConverters(
    LocalDateTimeConverter::class,
    CountConverter::class
)
@Entity(
    tableName = "book_records",
    primaryKeys = ["book_id", "date"],
    indices = [Index(value = ["date"])]
)
data class BookRecordEntity(
    @ColumnInfo(name = "book_id")
    val bookId: String,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "reading_time_count")
    val readingTimeCount: Count,
    @ColumnInfo(name = "read_count")
    val readCount: Int,
    @ColumnInfo(name = "is_finished")
    val isFinished: Boolean = false
)
