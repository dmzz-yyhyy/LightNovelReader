package indi.dmzz_yyhyy.lightnovelreader.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import indi.dmzz_yyhyy.lightnovelreader.data.`object`.ChapterContent

@Dao
abstract class BookChapterContentDao {
    @Query("SELECT title, content FROM bookchaptercontent WHERE id=:contentId LIMIT 1")
    abstract suspend fun getByContentId(contentId: Int): ChapterContent

    @Query("SELECT title, content FROM bookchaptercontent WHERE id=:id")
    abstract suspend fun getByBookId(id: Int): List<ChapterContent>

    @Query("INSERT INTO bookchaptercontent VALUES (:contentId, :bookId, :title, :content)")
    abstract suspend fun add(contentId: Int, bookId: Int, title: String, content: String)
    @Query("DELETE FROM bookchaptercontent WHERE id=:id")
    abstract suspend fun delete(id: Int)
}