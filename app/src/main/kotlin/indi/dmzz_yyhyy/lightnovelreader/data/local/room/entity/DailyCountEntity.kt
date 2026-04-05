package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.CountConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.serializer.CountSerializer
import indi.dmzz_yyhyy.lightnovelreader.data.serializer.LocalDateSerializer
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
@TypeConverters(
    LocalDateTimeConverter::class,
    CountConverter::class
)
@Entity(tableName = "daily_count")
data class DailyCountEntity(
    @Serializable(LocalDateSerializer::class)
    @PrimaryKey
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @Serializable(CountSerializer::class)
    @ColumnInfo(name = "time_count")
    val timeCount: Count,
) : Mergeable<DailyCountEntity> {
    override fun merge(new: DailyCountEntity): DailyCountEntity =
        DailyCountEntity(date = date, timeCount = timeCount + new.timeCount)
}
