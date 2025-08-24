package indi.dmzz_yyhyy.lightnovelreader.data.extensions.model

import kotlinx.serialization.Serializable

/**
 * Represents a search result from an extension
 */
@Serializable
data class ExtensionSearchResult(
    val id: String,
    val title: String,
    val author: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val url: String = "",
    val extensionId: String = "" // ID of the extension that provided this result
)

/**
 * Represents a book from an extension
 */
@Serializable
data class ExtensionBook(
    val id: String,
    val title: String,
    val author: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val url: String = "",
    val genres: List<String> = emptyList(),
    val status: String = "",
    val lastUpdated: Long = 0
)

/**
 * Represents a chapter from an extension
 */
@Serializable
data class ExtensionChapter(
    val id: String,
    val title: String,
    val content: String = "",
    val url: String = "",
    val order: Int = 0,
    val releaseDate: Long = 0
)
