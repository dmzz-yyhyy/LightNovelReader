package indi.dmzz_yyhyy.lightnovelreader.data.extensions.model

/**
 * Represents a chapter from an extension
 */
data class ExtensionChapter(
    val id: String,
    val title: String,
    val content: String = "",
    val url: String = "",
    val order: Int = 0,
    val releaseDate: Long = 0
)
