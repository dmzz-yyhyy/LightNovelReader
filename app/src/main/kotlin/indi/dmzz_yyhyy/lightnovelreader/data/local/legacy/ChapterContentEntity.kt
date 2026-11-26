package indi.dmzz_yyhyy.lightnovelreader.data.local.legacy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChapterContentEntity(

    val id: String,
    val title: String,

    val content: String,
    @SerialName("lastChapter")
    val prevChapter: String,
    val nextChapter: String
)
