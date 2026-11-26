package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupGroup
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupTable
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import io.nightfish.lightnovelreader.api.identifier.Identifier
import java.time.LocalDateTime

@TypeConverters(LocalDateTimeConverter::class, ListConverter::class)
@Entity(tableName = "book_shelf_book_metadata")
@BackupTable(BackupGroup.BOOKSHELF, "id", "book")
data class BookshelfBookMetadataEntity(
    @PrimaryKey
    val id: Identifier.Book,
    @ColumnInfo(name = "last_update")
    val lastUpdate: LocalDateTime,
    @ColumnInfo(name = "book_shelf_ids")
    val bookShelfIds: List<Identifier.Bookshelf>,
) : Mergeable<BookshelfBookMetadataEntity> {
    override fun merge(new: BookshelfBookMetadataEntity): BookshelfBookMetadataEntity =
        BookshelfBookMetadataEntity(
            id = new.id,
            lastUpdate = this.lastUpdate.coerceAtLeast(new.lastUpdate),
            bookShelfIds = (this.bookShelfIds + new.bookShelfIds).distinct()
        )
}
