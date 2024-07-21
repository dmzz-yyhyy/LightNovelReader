package indi.dmzz_yyhyy.lightnovelreader.data.local.room.eneity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.IntListConverter

@TypeConverters(IntListConverter::class)
@Entity(tableName = "volume")
data class VolumeEntity(
    @ColumnInfo(name = "book_id")
    val bookId: Int,
    @PrimaryKey
    @ColumnInfo(name = "volume_id")
    val volumeId: Int,
    @ColumnInfo(name = "volume_title")
    val volumeTitle: String,
    @ColumnInfo(name = "chapter_id_list")
    val chapterIds: List<Int>,
)
