package indi.dmzz_yyhyy.lightnovelreader.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BookChapter(
    @PrimaryKey val id: Int,
    val bookId: Int,
    val bookName: String,
    val volumeId: Int,
)