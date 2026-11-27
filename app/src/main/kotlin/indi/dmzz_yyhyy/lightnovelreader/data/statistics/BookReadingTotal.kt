package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.coroutines.flow.Flow

data class BookReadingTotal(
    val book: Identifier.Book,
    val bookInformationFlow: Flow<Result<BookInformation, WebRequestError>>,
    val seconds: Long
)
