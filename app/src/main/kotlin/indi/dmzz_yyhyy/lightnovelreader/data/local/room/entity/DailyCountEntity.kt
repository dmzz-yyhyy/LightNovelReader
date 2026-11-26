package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupGroup
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupTable
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.CountConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import java.time.LocalDate

@TypeConverters(
    LocalDateTimeConverter::class,
    CountConverter::class
)
@Entity(tableName = "daily_count", primaryKeys = ["source_namespace", "date"])
@BackupTable(BackupGroup.READING, "source_namespace", "")
data class DailyCountEntity(
    @ColumnInfo(name = "source_namespace")
    val sourceNamespace: String,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "time_count")
    val timeCount: Count,
) : Mergeable<DailyCountEntity> {
    override fun merge(new: DailyCountEntity): DailyCountEntity =
        DailyCountEntity(
            sourceNamespace = sourceNamespace,
            date = date,
            timeCount = timeCount + new.timeCount
        )
}
