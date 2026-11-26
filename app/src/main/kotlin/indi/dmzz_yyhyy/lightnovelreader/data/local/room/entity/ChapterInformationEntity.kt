package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupGroup
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupTable
import io.nightfish.lightnovelreader.api.identifier.Identifier

@Entity(tableName = "chapter_information")
@BackupTable(BackupGroup.CACHE, "id", "chapter")
data class ChapterInformationEntity(
    @PrimaryKey
    val id: Identifier.Chapter,
    val title: String
) : Mergeable<ChapterInformationEntity> {
    override fun merge(new: ChapterInformationEntity): ChapterInformationEntity = new
}
