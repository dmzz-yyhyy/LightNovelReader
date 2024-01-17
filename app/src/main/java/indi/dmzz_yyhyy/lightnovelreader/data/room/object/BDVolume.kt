package indi.dmzz_yyhyy.lightnovelreader.data.room.`object`

import androidx.room.Embedded
import androidx.room.Relation
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.Volume
import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.BookChapter
import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.BookVolume

data class BDVolume(
    @Embedded val bDVolume: BookVolume,
    @Relation(
        parentColumn = "id",
        entityColumn = "id"
    )
    val bDChapterList: List<BookChapter>,
) {
    fun volume(): Volume {
        val chapterList = ArrayList<Volume.Chapter>()
        for (chapter in bDChapterList) {
            chapterList.add(Volume.Chapter(chapter.bookName, chapter.id))
        }
        return Volume(bDVolume.volumeName, chapterList)
    }
}