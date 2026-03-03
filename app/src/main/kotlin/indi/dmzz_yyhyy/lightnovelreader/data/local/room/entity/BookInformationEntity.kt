package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.LocalDateTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.UriConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.WorldCountConverter
import indi.dmzz_yyhyy.lightnovelreader.data.serialier.LocalDateTimeSerializer
import indi.dmzz_yyhyy.lightnovelreader.data.serialier.UriSerializer
import io.nightfish.lightnovelreader.api.book.WordCount
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
@TypeConverters(
    LocalDateTimeConverter::class,
    ListConverter::class,
    WorldCountConverter::class,
    UriConverter::class
)
@Entity(tableName = "book_information")
data class BookInformationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subtitle: String,
    @Serializable(UriSerializer::class)
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
    @Serializable(LocalDateTimeSerializer::class)
    val lastUpdated: LocalDateTime,
    @ColumnInfo(name = "is_complete")
    val isComplete: Boolean
): Mergeable<BookInformationEntity> {
    override fun merge(
        new: BookInformationEntity
    ): BookInformationEntity =
        if (this.lastUpdated.isBefore(new.lastUpdated))
            new
        else this
}
