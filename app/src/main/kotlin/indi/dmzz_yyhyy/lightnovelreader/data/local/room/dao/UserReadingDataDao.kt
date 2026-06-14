package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ChapterReadingProgressMapConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserReadingDataEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UserReadingDataDao {
    @TypeConverters(ChapterReadingProgressMapConverter::class, LocalDateTimeConverter::class)
    @Query("replace into user_reading_data (id, last_read_time, total_read_time, reading_progress, last_read_chapter_id, last_read_chapter_title, current_chapter_reading_progress_map, max_chapter_reading_progress_map) " +
            "values (:id, :lastReadTime, :totalReadTime, :readingProgress, :lastReadChapterId, :lastReadChapterTitle, :currentChapterReadingProgressMap, :maxChapterReadingProgressMap)")
    fun insert(
        id: String,
        lastReadTime: LocalDateTime,
        totalReadTime: Int,
        readingProgress: Float,
        lastReadChapterId: String,
        lastReadChapterTitle: String,
        currentChapterReadingProgressMap: Map<String, Float>,
        maxChapterReadingProgressMap: Map<String, Float>
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userReading: UserReadingDataEntity)

    @Query("select * from user_reading_data where id = :id")
    fun getEntity(id: String): UserReadingDataEntity?

    @Query("select * from user_reading_data where id = :id")
    fun getEntityFlow(id: String): Flow<UserReadingDataEntity?>

    @Query("select * from user_reading_data")
    fun getAll(): List<UserReadingDataEntity>

    @Query("select * from user_reading_data where id = :id")
    fun getEntityWithoutFlow(id: String): UserReadingDataEntity?

    @Query("delete from user_reading_data where id in (:ids)")
    fun deleteByIds(ids: List<String>)

    @Query("delete from user_reading_data")
    fun clear()
}
