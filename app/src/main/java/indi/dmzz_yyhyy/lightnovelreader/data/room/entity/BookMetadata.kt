package indi.dmzz_yyhyy.lightnovelreader.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.room.converter.StringListConverter

@Entity
@TypeConverters(StringListConverter::class)
data class BookMetadata(
    @PrimaryKey val id: Int,
    var name: String,
    val coverUrl: String,
    var writer: String,
    var type: String,
    var tags: List<String>,
    var introduction: String,
    var lastReadChapterId: Int = -1,
)
