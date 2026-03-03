package io.nightfish.lightnovelreader.api.book

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDateTime

@Stable
interface BookInformation: CanBeEmpty, Copyable<BookInformation> {
    val id: String
    val title: String
    val subtitle: String
    val coverUri: Uri
    val author: String
    val description: String
    val tags: List<String>
    val publishingHouse: String
    val wordCount: WordCount
    val lastUpdated: LocalDateTime
    val isComplete: Boolean

    companion object {
        fun empty(): BookInformation = empty("")
        fun empty(id: String): BookInformation = MutableBookInformation(
            id,
            "",
            "",
            Uri.EMPTY,
            "",
            "",
            emptyList(),
            "",
            WordCount(0),
            LocalDateTime.MIN,
            false
        )
    }

    override fun isEmpty() = id.isEmpty() || title == ""

    override fun copy(): BookInformation =
        MutableBookInformation(id, title, subtitle, coverUri, author, description, tags, publishingHouse, wordCount, lastUpdated, isComplete)


    fun toMutable(): MutableBookInformation {
        if (this is MutableBookInformation)
            return this
        return MutableBookInformation(id, title, subtitle, coverUri, author, description, tags, publishingHouse, wordCount, lastUpdated, isComplete)
    }
}

class MutableBookInformation(
    id: String,
    title: String,
    subtitle: String,
    coverUrl: Uri,
    author: String,
    description: String,
    tags: List<String>,
    publishingHouse: String,
    wordCount: WordCount,
    lastUpdated: LocalDateTime,
    isComplete: Boolean
): BookInformation {
    override var id by mutableStateOf(id)
    override var title by mutableStateOf(title)
    override var subtitle  by mutableStateOf(subtitle)
    override var coverUri by mutableStateOf(coverUrl)
    override var author by mutableStateOf(author)
    override var description by mutableStateOf(description)
    override val tags = mutableStateListOf<String>().apply { addAll(tags) }
    override var publishingHouse by mutableStateOf(publishingHouse)
    override var wordCount by mutableStateOf(wordCount)
    override var lastUpdated by mutableStateOf(lastUpdated)
    override var isComplete by mutableStateOf(isComplete)

    companion object {
        fun empty(): MutableBookInformation = MutableBookInformation(
            "",
            "",
            "",
            Uri.EMPTY,
            "",
            "",
            emptyList(),
            "",
            WordCount(0),
            LocalDateTime.MIN,
            false
        )
    }
    
    fun update(bookInformation: BookInformation) {
        this.id = bookInformation.id
        this.title = bookInformation.title
        this.subtitle = bookInformation.subtitle
        this.coverUri = bookInformation.coverUri
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
