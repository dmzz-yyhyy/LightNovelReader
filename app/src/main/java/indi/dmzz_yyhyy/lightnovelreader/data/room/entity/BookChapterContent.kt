package indi.dmzz_yyhyy.lightnovelreader.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookChapterContent(
    @PrimaryKey val id: Int,
    val bookId: Int,
    val title: String,
    val content: String,
)