package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent

@Dao
interface ChapterContentDao {
    @Query("replace into chapter_content (id, title, content, lastChapter, nextChapter) " +
            "values (:id, :title, :content, :lastChapter, :nextChapter)")
    suspend fun update(id: Int, title: String, content: String, lastChapter: Int, nextChapter: Int)

    @Transaction
    suspend fun update(chapterContent: ChapterContent) {
        update(
            chapterContent.id,
            chapterContent.title,
            chapterContent.content,
            chapterContent.lastChapter,
            chapterContent.nextChapter
        )
    }

    @Query("select * from chapter_content where id = :id")
    suspend fun get(id: Int): ChapterContent?
}
