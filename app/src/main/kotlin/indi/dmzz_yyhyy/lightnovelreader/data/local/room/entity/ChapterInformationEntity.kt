package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "chapter_information")
data class ChapterInformationEntity(
    @PrimaryKey
    val id: String,
    val title: String
): Mergeable<ChapterInformationEntity> {
    override fun merge(new: ChapterInformationEntity): ChapterInformationEntity = new
}