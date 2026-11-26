package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupGroup
import indi.dmzz_yyhyy.lightnovelreader.data.local.backup.BackupTable
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.JsonObjectConverter
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonObject

@TypeConverters(
    JsonObjectConverter::class
)
@Entity(tableName = "chapter_content")
@BackupTable(BackupGroup.CACHE, "id", "chapter")
data class ChapterContentEntity(
    @PrimaryKey
    val id: Identifier.Chapter,
    val title: String,
    val content: JsonObject,
    @SerialName("lastChapter")
    @ColumnInfo(name = "lastChapter")
    val prevChapter: Identifier.Chapter?,
    val nextChapter: Identifier.Chapter?
) : Mergeable<ChapterContentEntity> {
    override fun merge(new: ChapterContentEntity): ChapterContentEntity = new
}
