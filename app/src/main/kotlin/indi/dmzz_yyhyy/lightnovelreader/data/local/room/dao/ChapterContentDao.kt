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
            "values (:id, :title, :content, :lastChapter, :nextChapter)")
    fun update(id: String, title: String, content: JsonObject, lastChapter: String, nextChapter: String)

    @Transaction
    fun update(chapterContent: ChapterContent) {
        update(
            chapterContent.id,
            chapterContent.title,
            chapterContent.content,
            chapterContent.lastChapter,
            chapterContent.nextChapter
        )
    }

    @Transaction
    fun update(chapterContent: ChapterContentEntity) {
        update(
            chapterContent.id,
            chapterContent.title,
            chapterContent.content,
            chapterContent.lastChapter,
            chapterContent.nextChapter
        )
    }

    @Query("select * from chapter_content where id = :id")
    suspend fun get(id: String): ChapterContentEntity?

    @Query("select id from chapter_content where id = :id")
    suspend fun getId(id: String): String?

    @Query("delete from chapter_content")
    fun clear()

    @Query("delete from chapter_content where id in (:ids)")
    fun deleteByIds(ids: List<String>)

    @Update
    fun updateEntities(vararg entities: ChapterContentEntity)

    @Query("select * from chapter_content")
    fun getAllEntities(): List<ChapterContentEntity>
}
