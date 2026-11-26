package indi.dmzz_yyhyy.lightnovelreader.data.local.legacy

import kotlinx.serialization.Serializable

@Serializable
data class UserReadingDataEntity(

    val id: String,

    val lastReadTime: String,
    val totalReadTime: Int,
    val readingProgress: Float,
    val lastReadChapterId: String,
    val lastReadChapterTitle: String,
    val currentChapterReadingProgressMap: Map<String, Float>,
    val maxChapterReadingProgressMap: Map<String, Float>
)
