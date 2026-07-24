package indi.dmzz_yyhyy.lightnovelreader.data.format

import androidx.compose.runtime.Stable
import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.flow.Flow

@Stable
data class FormattingGroup(
    val id: String,
    val bookInformationFlow: Flow<Result<BookInformation, WebRequestError>>,
    val size: Int
)
