package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import androidx.compose.runtime.Stable
import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

@Stable
data class BookRecord(
    val bookId: String,
    val bookInformationFlow: Flow<Result<BookInformation, WebRequestError>>,
    val date: LocalDate,
    val reads: Int,
    val seconds: Int,
    val isFinished: Boolean = false,
    val isFavorited: Boolean = false,
    val firstSeen: LocalTime,
    val lastSeen: LocalTime,
)
