package indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import androidx.room.Update
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.converter.JsonObjectConverter
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.ChapterContentEntity
import io.nightfish.lightnovelreader.api.book.ChapterContent
import kotlinx.serialization.json.JsonObject

@Dao
interface ChapterContentDao {
    @TypeConverters(JsonObjectConverter::class)
    @Query("replace into chapter_content (id, title, content, lastChapter, nextChapter) " +
            "values (:id, :title, :content, :prevChapter, :nextChapter)"
    )
    suspend fun update(id: String, title: String, content: JsonObject, prevChapter: String, nextChapter: String)

    @Transaction
    suspend fun update(chapterContent: ChapterContent) {
        update(
            chapterContent.id,
            chapterContent.title,
            chapterContent.content,
            chapterContent.prevChapter ?: "",
            chapterContent.nextChapter ?: ""
        )
    }

    @Transaction
    suspend fun update(chapterContent: ChapterContentEntity) {
        update(
            chapterContent.id,
            chapterContent.title,
            chapterContent.content,
            chapterContent.prevChapter,
            chapterContent.nextChapter
        )
    }

    @Query("select * from chapter_content where id = :id")
    suspend fun get(id: String): ChapterContentEntity?

    @Query("select id from chapter_content where id = :id")
    suspend fun getId(id: String): String?

    @Query("delete from chapter_content")
    suspend fun clear()

    @Query("delete from chapter_content where id in (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Update
    suspend fun updateEntities(vararg entities: ChapterContentEntity)

    @Query("select * from chapter_content")
    suspend fun getAllEntities(): List<ChapterContentEntity>
}
