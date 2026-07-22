package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.runtime.Stable
import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.flow.Flow

@Stable
data class ChapterSheetUiState(
    val bookId: String,
    val readingChapterId: String,
    val selectedVolumeId: String = "",
    val bookVolumeFlow: Flow<Result<BookVolumes, WebRequestError>>
)