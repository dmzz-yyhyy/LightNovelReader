package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.runtime.Stable
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.UserReadingData

@Stable
data class RecentReadingBook(
    val id: String,
    val bookInformation: BookInformation,
    val userReadingData: UserReadingData
)