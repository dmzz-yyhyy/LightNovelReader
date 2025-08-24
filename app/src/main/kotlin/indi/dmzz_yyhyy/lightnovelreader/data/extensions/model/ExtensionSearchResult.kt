package indi.dmzz_yyhyy.lightnovelreader.data.extensions.model

/**
 * Represents a search result from an extension
 */
data class ExtensionSearchResult(
    val id: String,
    val title: String,
    val author: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val url: String = "",
    val extensionId: String = "" // ID of the extension that provided this result
)
