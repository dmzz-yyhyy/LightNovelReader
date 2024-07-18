package indi.dmzz_yyhyy.lightnovelreader.data.local.room.eneity

import java.time.LocalDateTime

data class BookInformation(
    val id: Int,
    val title: String,
    val coverUrl: String,
    val author: String,
    val description: String,
    val publishingHouse: String,
    val wordCount: Int,
    val lastUpdated: LocalDateTime,
    val isComplete: Boolean
)
