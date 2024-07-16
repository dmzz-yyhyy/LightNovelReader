package indi.dmzz_yyhyy.lightnovelreader.data.book

import java.time.LocalDateTime

data class BookInformation(
    val id: Int,
    val title: String,
    val coverUrl: String,
    val author: String,
    val description: String,
    val tags: List<String>,
    val publishingHouse: String,
    val wordCount: Int,
    val lastUpdated: LocalDateTime,
    val isComplete: Boolean,
)
