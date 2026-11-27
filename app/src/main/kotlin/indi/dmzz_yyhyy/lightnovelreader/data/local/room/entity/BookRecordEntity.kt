package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupGroup
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupTable
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import io.nightfish.lightnovelreader.api.identifier.Identifier
import java.time.LocalDate
import java.time.LocalTime

@TypeConverters(LocalDateTimeConverter::class)
@Entity(
    tableName = "book_records",
    primaryKeys = ["book_id", "date"],
    indices = [Index(value = ["date"])]
)
@BackupTable(BackupGroup.READING, "book_id", "book")
data class BookRecordEntity(
    @ColumnInfo(name = "book_id")
    val bookId: Identifier.Book,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "reads")
    val reads: Int,
    @ColumnInfo(name = "seconds")
    val seconds: Int,
    @ColumnInfo(name = "is_finished")
    val isFinished: Boolean = false,
    @ColumnInfo(name = "is_favorited")
    val isFavorited: Boolean = false,
    @ColumnInfo(name = "first_seen")
    val firstSeen: LocalTime,
    @ColumnInfo(name = "last_seen")
    val lastSeen: LocalTime,
) : Mergeable<BookRecordEntity> {
    override fun merge(new: BookRecordEntity): BookRecordEntity =
        BookRecordEntity(
            bookId = bookId,
            date = date,
            reads = reads + new.reads,
            seconds = seconds + new.seconds,
            isFinished = isFinished || new.isFinished,
            isFavorited = isFavorited || new.isFavorited,
            firstSeen = if (firstSeen.isBefore(new.firstSeen)) firstSeen else new.firstSeen,
            lastSeen = if (lastSeen.isAfter(new.lastSeen)) lastSeen else new.lastSeen,
        )
}
