package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ChapterReadingProgressMapConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.UserReadingDataEntity
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UserReadingDataDao {
    @TypeConverters(ChapterReadingProgressMapConverter::class, LocalDateTimeConverter::class)
    @Query(
        "replace into user_reading_data (id, last_read_time, total_read_time, reading_progress, last_read_chapter_id, last_read_chapter_title, current_chapter_reading_progress_map, max_chapter_reading_progress_map) " +
                "values (:id, :lastReadTime, :totalReadTime, :readingProgress, :lastReadChapterId, :lastReadChapterTitle, :currentChapterReadingProgressMap, :maxChapterReadingProgressMap)"
    )
    suspend fun insert(
        id: Identifier.Book,
        lastReadTime: LocalDateTime,
        totalReadTime: Int,
        readingProgress: Float,
        lastReadChapterId: Identifier.Chapter?,
        lastReadChapterTitle: String,
        currentChapterReadingProgressMap: Map<Identifier.Chapter, Float>,
        maxChapterReadingProgressMap: Map<Identifier.Chapter, Float>
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userReading: UserReadingDataEntity)

    @Query("select * from user_reading_data where id = :id")
    suspend fun getEntity(id: Identifier.Book): UserReadingDataEntity?

    @Query("select * from user_reading_data where id = :id")
    fun getEntityFlow(id: Identifier.Book): Flow<UserReadingDataEntity?>

    @Query("select * from user_reading_data WHERE id GLOB 'book:' || :namespace || ':*'")
    suspend fun getAll(namespace: String): List<UserReadingDataEntity>

    @Query("delete from user_reading_data where id in (:ids)")
    suspend fun deleteByIds(ids: List<Identifier.Book>)

    @Query("delete from user_reading_data WHERE id GLOB 'book:' || :namespace || ':*'")
    suspend fun clear(namespace: String)
}
