package indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity

import androidx.room.ColumnInfo
import io.nightfish.lightnovelreader.api.identifier.Identifier
import java.time.LocalDate

data class BookDate(
    @ColumnInfo(name = "book_id")
    val bookId: Identifier.Book,
    @ColumnInfo(name = "date")
    val date: LocalDate
)
