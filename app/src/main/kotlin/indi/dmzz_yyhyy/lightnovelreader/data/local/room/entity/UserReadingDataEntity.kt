package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ChapterReadingProgressMapConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.serialier.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
@TypeConverters(ListConverter::class, LocalDateTimeConverter::class, ChapterReadingProgressMapConverter::class)
@Entity(tableName = "user_reading_data")
data class UserReadingDataEntity(
    @PrimaryKey
    val id: String,
    @Serializable(LocalDateTimeSerializer::class)
    @ColumnInfo(name = "last_read_time")
    val lastReadTime: LocalDateTime,
    @ColumnInfo(name = "total_read_time")
    val totalReadTime: Int,
    @ColumnInfo(name = "reading_progress")
    val readingProgress: Float,
    @ColumnInfo(name = "last_read_chapter_id")
    val lastReadChapterId: String,
    @ColumnInfo(name = "last_read_chapter_title")
    val lastReadChapterTitle: String,
    @ColumnInfo(name = "current_chapter_reading_progress_map")
    val currentChapterReadingProgressMap: Map<String, Float>,
    @ColumnInfo(name = "max_chapter_reading_progress_map")
    val maxChapterReadingProgressMap: Map<String, Float>
): Mergeable<UserReadingDataEntity> {
    override fun merge(new: UserReadingDataEntity): UserReadingDataEntity = new.copy(
        lastReadTime = new.lastReadTime.coerceAtLeast(this.lastReadTime),
        totalReadTime = new.totalReadTime.coerceAtLeast(this.totalReadTime),
        readingProgress = new.readingProgress.coerceAtLeast(this.readingProgress),
        maxChapterReadingProgressMap = new.maxChapterReadingProgressMap.mapValues { (key, value) ->
            value.coerceAtLeast(this.maxChapterReadingProgressMap[key] ?: 0f)
        }
    )
}
