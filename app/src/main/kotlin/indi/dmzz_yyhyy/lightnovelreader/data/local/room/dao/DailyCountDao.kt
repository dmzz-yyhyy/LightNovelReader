package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.DailyCountEntity
import java.time.LocalDate

@Dao
@TypeConverters(LocalDateTimeConverter::class)
interface DailyCountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DailyCountEntity)

    @Query("SELECT * FROM daily_count WHERE date = :date AND source_namespace = :namespace")
    suspend fun getByDate(date: LocalDate, namespace: String): DailyCountEntity?

    @Query("SELECT * FROM daily_count WHERE date BETWEEN :start AND :end AND source_namespace = :namespace")
    suspend fun getBetween(
        start: LocalDate,
        end: LocalDate,
        namespace: String
    ): List<DailyCountEntity>

    @Query("SELECT * FROM daily_count WHERE source_namespace = :namespace")
    suspend fun getAll(namespace: String): List<DailyCountEntity>

    @Query("SELECT * FROM daily_count WHERE date = :date AND source_namespace = :namespace")
    suspend fun getEntity(date: LocalDate, namespace: String): DailyCountEntity?

    @Query("DELETE FROM daily_count WHERE source_namespace = :namespace")
    suspend fun clear(namespace: String)
}
