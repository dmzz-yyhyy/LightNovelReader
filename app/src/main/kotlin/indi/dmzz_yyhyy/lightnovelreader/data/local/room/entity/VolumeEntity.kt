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
@Entity(tableName = "volume")
@BackupTable(BackupGroup.CACHE, "book_id", "book")
data class VolumeEntity(
    @ColumnInfo(name = "book_id")
    val bookId: Identifier.Book,
    @PrimaryKey
    @ColumnInfo(name = "volume_id")
    val volumeId: Identifier.Volume,
    @ColumnInfo(name = "volume_title")
    val volumeTitle: String,
    @ColumnInfo(name = "chapter_id_list")
    val chapterIds: List<Identifier.Chapter>,
    @ColumnInfo(name = "volume_index")
    val index: Int
) : Mergeable<VolumeEntity> {
    override fun merge(new: VolumeEntity): VolumeEntity = new
}
