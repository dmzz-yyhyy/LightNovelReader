package indi.dmzz_yyhyy.lightnovelreader.data.local.room.eneity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.loacltion.room.converter.LocalDataTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.IntListConverter
import java.time.LocalDateTime

@TypeConverters(IntListConverter::class, LocalDataTimeConverter::class)
@Entity(tableName = "user_reading_data")
data class UserReadingDataEntity(
    @PrimaryKey
    val id: Int,
    @ColumnInfo(name = "last_read_time")
    val lastReadTime: LocalDateTime,
    @ColumnInfo(name = "total_read_time")
    val totalReadTime: Int,
    @ColumnInfo(name = "reading_progress")
    val readingProgress: Float,
    @ColumnInfo(name = "last_read_chapter_id")
    val lastReadChapterId: Int,
    @ColumnInfo(name = "last_read_chapter_title")
    val lastReadChapterTitle: String,
    @ColumnInfo(name = "last_read_chapter_progress")
    val lastReadChapterProgress: Float
)
