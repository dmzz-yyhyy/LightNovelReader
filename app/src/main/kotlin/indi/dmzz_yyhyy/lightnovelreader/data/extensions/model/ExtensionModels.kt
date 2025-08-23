package indi.dmzz_yyhyy.lightnovelreader.data.extensions.model

import kotlinx.serialization.Serializable

@Serializable
data class ExtensionSearchResult(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val wordCount: Int = 0,
    val isComplete: Boolean = false
)

@Serializable
data class ExtensionBook(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val wordCount: Int = 0,
    val isComplete: Boolean = false,
    val chapters: List<ExtensionChapter> = emptyList()
)

@Serializable
data class ExtensionChapter(
    val id: String,
    val title: String,
    val content: String = "",
    val url: String = "",
    val order: Int = 0
)
