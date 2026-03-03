package io.nightfish.lightnovelreader.plugin.js.api.book

import android.net.Uri
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class JsBookInformation(
    override val id: String,
    override val title: String,
    override val subtitle: String,
    override val coverUri: Uri,
    override val author: String,
    override val description: String,
    override val tags: List<String>,
    override val publishingHouse: String,
    override val wordCount: WordCount,
    val lastUpdatedZonedDateTime: ZonedDateTime,
    override val isComplete: Boolean
): BookInformation {
    override val lastUpdated: LocalDateTime = LocalDateTime.ofInstant(lastUpdatedZonedDateTime.toInstant(), ZoneId.systemDefault())

    companion object {
        fun empty(id: String): JsBookInformation {
            return JsBookInformation(id, "", "", Uri.EMPTY, "", "", emptyList(), "", WordCount(-1), ZonedDateTime.now(), false)
        }
    }
}
