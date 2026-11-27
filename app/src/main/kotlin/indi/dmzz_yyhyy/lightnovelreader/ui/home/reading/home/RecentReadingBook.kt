package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.runtime.Stable
import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.UserReadingData
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier

@Stable
data class RecentReadingBook(
    val id: Identifier.Book,
    val bookInformationResult: Result<BookInformation, WebRequestError>,
    val userReadingData: UserReadingData,
)
