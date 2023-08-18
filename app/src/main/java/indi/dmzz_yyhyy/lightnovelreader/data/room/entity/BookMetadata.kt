package indi.dmzz_yyhyy.lightnovelreader.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookMetadata(
    @PrimaryKey val bookId: Int,
    val title: String,
    val coverUrl: String,
    val writer: String,
    val type: String,
    val tags: List<String>,
    val introduction: String
)
