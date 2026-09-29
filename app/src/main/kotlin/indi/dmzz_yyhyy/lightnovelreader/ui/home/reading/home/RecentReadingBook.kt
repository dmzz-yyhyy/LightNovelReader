package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.runtime.Stable
import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.UserReadingData
import io.nightfish.lightnovelreader.api.error.WebRequestError

@Stable
data class RecentReadingBook(
    val id: String,
    val bookInformationResult: Result<BookInformation, WebRequestError>,
    val userReadingData: UserReadingData,
)
