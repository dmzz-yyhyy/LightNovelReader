package io.nightfish.lightnovelreader.plugin.js.api.book

import java.time.ZonedDateTime

data class JsBookInformation(
    val id: Int,
    val title: String,
    val subtitle: String,
    val coverUrl: String,
    val author: String,
    val description: String,
    val tags: List<String>,
    val publishingHouse: String,
    val wordCount: Int,
    val lastUpdated: ZonedDateTime,
    val isComplete: Boolean
) {
    companion object {
        @JvmStatic
        fun empty(id: Int): JsBookInformation {
            return JsBookInformation(
                id,
                "",
                "",
                "",
                "",
                "",
                emptyList(),
                "",
                -1,
                ZonedDateTime.now(),
                false
            )
        }
    }
}
