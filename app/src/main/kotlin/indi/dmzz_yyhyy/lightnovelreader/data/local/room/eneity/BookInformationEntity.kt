package indi.dmzz_yyhyy.lightnovelreader.data.local.room.eneity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.loacltion.room.converter.LocalDataTimeConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import java.time.LocalDateTime

@TypeConverters(LocalDataTimeConverter::class, ListConverter::class)
@Entity(tableName = "book_information")
data class BookInformationEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    @ColumnInfo(name = "cover_url")
    val coverUrl: String,
    val author: String,
    val description: String,
    val tags: List<String>,
    @ColumnInfo(name = "publishing_house")
    val publishingHouse: String,
    @ColumnInfo(name = "word_count")
    val wordCount: Int,
    @ColumnInfo(name = "last_update")
    val lastUpdated: LocalDateTime,
    @ColumnInfo(name = "is_complete")
    val isComplete: Boolean
)
