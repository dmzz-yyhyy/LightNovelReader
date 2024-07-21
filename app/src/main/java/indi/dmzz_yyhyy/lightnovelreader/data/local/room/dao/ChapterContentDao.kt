package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent

@Dao
interface ChapterContentDao {
    @Query("replace into chapter_content (id, title, content) values (:id, :title, :content)")
    suspend fun update(id: Int, title: String, content: String)

    @Transaction
    suspend fun update(chapterContent: ChapterContent) {
        update(chapterContent.id, chapterContent.title, chapterContent.content)
    }

    @Query("select * from chapter_content where id = :id")
    suspend fun get(id: Int): ChapterContent?
}
