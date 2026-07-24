package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import java.time.LocalDate

data class BookDate(
    @ColumnInfo(name = "book_id")
    val bookId: String,
    @ColumnInfo(name = "date")
    val date: LocalDate
)