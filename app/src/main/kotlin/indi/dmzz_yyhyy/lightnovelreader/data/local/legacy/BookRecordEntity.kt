package indi.dmzz_yyhyy.lightnovelreader.data.local.legacy

import kotlinx.serialization.Serializable

@Serializable
data class BookRecordEntity(
    val bookId: String,

    val date: String,
    val reads: Int,
    val seconds: Int,
    val isFinished: Boolean = false,
    val isFavorited: Boolean = false,

    val firstSeen: String,

    val lastSeen: String,
)
