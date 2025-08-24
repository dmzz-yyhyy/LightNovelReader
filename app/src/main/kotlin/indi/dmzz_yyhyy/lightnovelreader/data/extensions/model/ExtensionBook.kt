package indi.dmzz_yyhyy.lightnovelreader.data.extensions.model

/**
 * Represents a book from an extension
 */
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
