package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import kotlinx.serialization.Serializable

@Serializable
@TypeConverters(ListConverter::class)
@Entity(tableName = "book_shelf")
data class BookshelfEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    @ColumnInfo(name = "sort_type")
    val sortType: String,
    @ColumnInfo(name = "auto_cache")
    val autoCache: Boolean,
    @ColumnInfo(name = "system_update_reminder")
    val systemUpdateReminder: Boolean,
    @ColumnInfo(name = "all_book_ids")
    val allBookIds: List<String>,
    @ColumnInfo(name = "pinned_book_ids")
    val pinnedBookIds: List<String>,
    @ColumnInfo(name = "updated_book_ids")
    val updatedBookIds: List<String>,
): Mergeable<BookshelfEntity> {
    override fun merge(new: BookshelfEntity): BookshelfEntity =
        BookshelfEntity(
            id = new.id,
            name = new.name,
            sortType = new.sortType,
            autoCache = new.autoCache,
            systemUpdateReminder = new.systemUpdateReminder,
            allBookIds = (this.allBookIds + new.allBookIds).distinct(),
            pinnedBookIds = (this.pinnedBookIds + new.pinnedBookIds).distinct(),
            updatedBookIds = (this.updatedBookIds + new.updatedBookIds).distinct()
        )
}
