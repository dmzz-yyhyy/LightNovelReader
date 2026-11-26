package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupGroup
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupTable
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import io.nightfish.lightnovelreader.api.identifier.Identifier

@TypeConverters(ListConverter::class)
@Entity(tableName = "book_shelf")
@BackupTable(BackupGroup.BOOKSHELF, "id", "bookshelf")
data class BookshelfEntity(
    @PrimaryKey
    val id: Identifier.Bookshelf,
    val name: String,
    @ColumnInfo(name = "sort_type")
    val sortType: String,
    @ColumnInfo(name = "sort_reversed")
    val sortReversed: Boolean = false,
    @ColumnInfo(name = "auto_cache")
    val autoCache: Boolean,
    @ColumnInfo(name = "system_update_reminder")
    val systemUpdateReminder: Boolean,
    @ColumnInfo(name = "all_book_ids")
    val allBookIds: List<Identifier.Book>,
    @ColumnInfo(name = "pinned_book_ids")
    val pinnedBookIds: List<Identifier.Book>,
    @ColumnInfo(name = "updated_book_ids")
    val updatedBookIds: List<Identifier.Book>,
) : Mergeable<BookshelfEntity> {
    override fun merge(new: BookshelfEntity): BookshelfEntity =
        BookshelfEntity(
            id = new.id,
            name = new.name,
            sortType = new.sortType,
            sortReversed = new.sortReversed,
            autoCache = new.autoCache,
            systemUpdateReminder = new.systemUpdateReminder,
            allBookIds = (this.allBookIds + new.allBookIds).distinct(),
            pinnedBookIds = (this.pinnedBookIds + new.pinnedBookIds).distinct(),
            updatedBookIds = (this.updatedBookIds + new.updatedBookIds).distinct()
        )
}
