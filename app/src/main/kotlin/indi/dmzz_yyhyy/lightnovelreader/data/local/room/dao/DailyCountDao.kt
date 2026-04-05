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
    fun insert(entity: DailyCountEntity)

    @Query("SELECT * FROM daily_count WHERE date = :date")
    suspend fun getByDate(date: LocalDate): DailyCountEntity?

    @Query("SELECT * FROM daily_count WHERE date BETWEEN :start AND :end")
    suspend fun getBetween(start: LocalDate, end: LocalDate): List<DailyCountEntity>

    @Query("SELECT * FROM daily_count")
    fun getAll(): List<DailyCountEntity>

    @Query("SELECT * FROM daily_count WHERE date = :date")
    fun getEntity(date: LocalDate): DailyCountEntity?

    @Query("DELETE FROM daily_count")
    fun clear()
}
