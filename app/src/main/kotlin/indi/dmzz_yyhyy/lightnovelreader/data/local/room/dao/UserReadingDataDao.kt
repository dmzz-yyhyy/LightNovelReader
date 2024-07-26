package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.data.loacltion.room.converter.LocalDataTimeConverter.dateToString
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.eneity.UserReadingDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserReadingDataDao {
    @Query("replace into user_reading_data (id, last_read_time, total_read_time, reading_progress, last_read_chapter_id, last_read_chapter_title, last_read_chapter_progress) " +
            "values (:id, :lastReadTime, :totalReadTime, :readingProgress, :lastReadChapterId, :lastReadChapterTitle, :lastReadChapterProgress)")
    suspend fun update(
        id: Int,
        lastReadTime: String,
        totalReadTime: Int,
        readingProgress: Float,
        lastReadChapterId: Int,
        lastReadChapterTitle: String,
        lastReadChapterProgress: Float
    )

    @Transaction
    suspend fun update(userReading: UserReadingData) {
        dateToString(userReading.lastReadTime)?.let {
            update(
                userReading.id,
                it,
                userReading.totalReadTime,
                userReading.readingProgress,
                userReading.lastReadChapterId,
                userReading.lastReadChapterTitle,
                userReading.lastReadChapterProgress
            )
        }
    }

    @Query("select * from user_reading_data where id = :id")
    fun getEntity(id: Int): Flow<UserReadingDataEntity?>

    //FiXME
    @Query("select * from user_reading_data")
    fun getAll(): List<UserReadingDataEntity>
}