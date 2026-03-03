package io.nightfish.lightnovelreader.api.book

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.nightfish.lightnovelreader.api.util.empty
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray

@Stable
interface ChapterContent: CanBeEmpty, Copyable<ChapterContent> {
    val id: String
    val title: String
    val content: JsonObject
    val lastChapter: String
    val nextChapter: String

    fun hasPrevChapter(): Boolean = lastChapter.isNotEmpty()
    fun hasNextChapter(): Boolean = nextChapter.isNotEmpty()

    override fun isEmpty() = this.id.isEmpty()
            || this.content.isEmpty()
            || this.content["components"]?.jsonArray?.isEmpty() ?: true


    override fun copy(): ChapterContent =
        MutableChapterContent(id, title, content, lastChapter, nextChapter)

    companion object {
        fun empty(): ChapterContent = empty("")
        fun empty(chapterId: String): ChapterContent = MutableChapterContent(
            chapterId,
            "",
            JsonObject.empty()
        )
    }

    fun toMutable(): MutableChapterContent {
        if (this is MutableChapterContent)
            return this
        return MutableChapterContent(id, title, content, lastChapter, nextChapter)
    }
}

class MutableChapterContent(
    id: String,
    title: String,
    content: JsonObject,
    lastChapter: String = "",
    nextChapter: String = ""
) : ChapterContent {
    override var id by mutableStateOf(id)
    override var title by mutableStateOf(title)
    override var content by mutableStateOf(content)
    override var lastChapter by mutableStateOf(lastChapter)
    override var nextChapter by mutableStateOf(nextChapter)

    companion object {
        fun empty(): MutableChapterContent = MutableChapterContent("", "", JsonObject.empty() )
    }

    fun update(chapterContent: ChapterContent) {
        this.id = chapterContent.id
        this.title = chapterContent.title
        this.content = chapterContent.content
        this.lastChapter = chapterContent.lastChapter
        this.nextChapter = chapterContent.nextChapter
    }

    override fun equals(other: Any?): Boolean {
        if (other is ChapterContent) {
            return this.id == other.id &&
                    this.content == other.content &&
                    this.lastChapter == other.lastChapter &&
                    this.nextChapter == other.nextChapter
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + lastChapter.hashCode()
        result = 31 * result + nextChapter.hashCode()
        return result
    }
}