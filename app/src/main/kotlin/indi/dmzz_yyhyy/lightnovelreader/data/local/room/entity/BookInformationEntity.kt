package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupGroup
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupTable
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.UriConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.WorldCountConverter
import io.nightfish.lightnovelreader.api.book.WordCount
import io.nightfish.lightnovelreader.api.identifier.Identifier
import java.time.LocalDateTime

@TypeConverters(
    LocalDateTimeConverter::class,
    ListConverter::class,
    WorldCountConverter::class,
    UriConverter::class
)
@Entity(tableName = "book_information")
@BackupTable(BackupGroup.CACHE, "id", "book")
data class BookInformationEntity(
    @PrimaryKey
    val id: Identifier.Book,
    val title: String,
    val subtitle: String,
    @ColumnInfo(name = "cover_uri")
    val coverUri: Uri,
    val author: String,
    val description: String,
    val tags: List<String>,
    @ColumnInfo(name = "publishing_house")
    val publishingHouse: String,
    @ColumnInfo(name = "word_count")
    val wordCount: WordCount,
    @ColumnInfo(name = "last_update")
    val lastUpdated: LocalDateTime,
    @ColumnInfo(name = "is_complete")
    val isComplete: Boolean
) : Mergeable<BookInformationEntity> {
    override fun merge(
        new: BookInformationEntity
    ): BookInformationEntity =
        if (this.lastUpdated.isBefore(new.lastUpdated))
            new
        else this
}
