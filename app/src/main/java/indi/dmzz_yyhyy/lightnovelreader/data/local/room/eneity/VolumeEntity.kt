package indi.dmzz_yyhyy.lightnovelreader.data.loacltion.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.IntListConverter

@TypeConverters(IntListConverter::class)
@Entity(tableName = "volume_entity")
data class VolumeEntity(
    @PrimaryKey
    @ColumnInfo(name = "volume_id")
    val volumeId: Int,
    @ColumnInfo(name = "volume_title")
    val volumeTitle: String,
    @ColumnInfo(name = "chapter_id_list")
    val chapterIds: List<Int>,
)
