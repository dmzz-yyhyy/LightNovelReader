package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.flow.Flow

data class DailyDateDetails(
    val formattedTotalTime: String,
    val timeDetails: List<Pair<Flow<Result<BookInformation, WebRequestError>>, Int>>
)