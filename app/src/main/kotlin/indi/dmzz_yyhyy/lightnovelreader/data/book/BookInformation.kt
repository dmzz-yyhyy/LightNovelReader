package indi.dmzz_yyhyy.lightnovelreader.data.book

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDateTime

@Stable
interface BookInformation {
    val id: Int
    val title: String
    val subtitle: String
    val coverUrl: String
    val author: String
    val description: String
    val tags: List<String>
    val publishingHouse: String
    val wordCount: Int
    val lastUpdated: LocalDateTime
    val isComplete: Boolean

    companion object {
        fun empty(): BookInformation = empty(-1)
        fun empty(id: Int): BookInformation = MutableBookInformation(
            id,
            "",
            "",
            "",
            "",
            "",
            emptyList(),
            "",
            0,
            LocalDateTime.MIN,
            false
        )
    }

    fun isEmpty() = id == -1 || title == ""

    @Suppress("unused")
    fun toMutable(): MutableBookInformation {
        if (this is MutableBookInformation)
            return this
        return MutableBookInformation(id, title, subtitle, coverUrl, author, description, tags, publishingHouse, wordCount, lastUpdated, isComplete)
    }
}

class MutableBookInformation(
    id: Int,
    title: String,
    subtitle: String,
    coverUrl: String,
    author: String,
    description: String,
    tags: List<String>,
    publishingHouse: String,
    wordCount: Int,
    lastUpdated: LocalDateTime,
    isComplete: Boolean
): BookInformation {
    override var id by mutableIntStateOf(id)
    override var title by mutableStateOf(title)
    override var subtitle  by mutableStateOf(subtitle)
    override var coverUrl by mutableStateOf(coverUrl)
    override var author by mutableStateOf(author)
    override var description by mutableStateOf(description)
    override val tags = mutableStateListOf<String>().apply { addAll(tags) }
    override var publishingHouse by mutableStateOf(publishingHouse)
    override var wordCount by mutableIntStateOf(wordCount)
    override var lastUpdated by mutableStateOf(lastUpdated)
    override var isComplete by mutableStateOf(isComplete)

    companion object {
        fun empty(): MutableBookInformation = MutableBookInformation(
            -1,
            "",
            "",
            "",
            "",
            "",
            emptyList(),
            "",
            0,
            LocalDateTime.MIN,
            false
        )
    }
    
    fun update(bookInformation: BookInformation) {
        this.id = bookInformation.id
        this.title = bookInformation.title
        this.subtitle = bookInformation.subtitle
        this.coverUrl = bookInformation.coverUrl
        this.author = bookInformation.author
        this.description = bookInformation.description
        this.tags.clear()
        this.tags.addAll(bookInformation.tags)
        this.publishingHouse = bookInformation.publishingHouse
        this.wordCount = bookInformation.wordCount
        this.lastUpdated = bookInformation.lastUpdated
        this.isComplete = bookInformation.isComplete
    }
}
