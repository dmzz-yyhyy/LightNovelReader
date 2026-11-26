package indi.dmzz_yyhyy.lightnovelreader.data.statistics

import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class DailyDateDetails(
    val date: LocalDate,
    val totalSeconds: Int,
    val readCount: Int,
    val hourMinutes: List<Int>,
    val books: List<DailyBookDetail>
)

data class DailyBookDetail(
    val bookId: String,
    val bookInformationFlow: Flow<Result<BookInformation, WebRequestError>>,
    val seconds: Int
)