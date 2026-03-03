package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.serialier.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
@TypeConverters(LocalDateTimeConverter::class, ListConverter::class)
@Entity(tableName = "book_shelf_book_metadata")
data class BookshelfBookMetadataEntity(
    @PrimaryKey
    val id: String,
    @Serializable(LocalDateTimeSerializer::class)
    @ColumnInfo(name = "last_update")
    val lastUpdate: LocalDateTime,
    @ColumnInfo(name = "book_shelf_ids")
    val bookShelfIds: List<Int>,
): Mergeable<BookshelfBookMetadataEntity> {
    override fun merge(new: BookshelfBookMetadataEntity): BookshelfBookMetadataEntity =
        BookshelfBookMetadataEntity(
            id = new.id,
            lastUpdate = this.lastUpdate.coerceAtLeast(new.lastUpdate),
            bookShelfIds = (this.bookShelfIds + new.bookShelfIds).distinct()
        )
}
