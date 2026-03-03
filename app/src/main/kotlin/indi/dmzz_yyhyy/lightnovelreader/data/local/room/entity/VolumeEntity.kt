package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.ListConverter
import kotlinx.serialization.Serializable

@Serializable
@TypeConverters(ListConverter::class)
@Entity(tableName = "volume")
data class VolumeEntity(
    @ColumnInfo(name = "book_id")
    val bookId: String,
    @PrimaryKey
    @ColumnInfo(name = "volume_id")
    val volumeId: String,
    @ColumnInfo(name = "volume_title")
    val volumeTitle: String,
    @ColumnInfo(name = "chapter_id_list")
    val chapterIds: List<String>,
    @ColumnInfo(name = "volume_index")
    val index: Int
): Mergeable<VolumeEntity> {
    override fun merge(new: VolumeEntity): VolumeEntity = new
}
