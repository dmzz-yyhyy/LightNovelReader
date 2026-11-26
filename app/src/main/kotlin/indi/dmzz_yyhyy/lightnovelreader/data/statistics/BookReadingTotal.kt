package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.flow.Flow

data class BookReadingTotal(
    val bookId: String,
    val bookInformationFlow: Flow<Result<BookInformation, WebRequestError>>,
    val seconds: Long
)
