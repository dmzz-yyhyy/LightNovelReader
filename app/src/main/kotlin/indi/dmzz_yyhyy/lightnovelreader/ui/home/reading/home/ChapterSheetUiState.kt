package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.runtime.Stable
import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.coroutines.flow.Flow

@Stable
data class ChapterSheetUiState(
    val bookId: Identifier.Book,
    val readingChapterId: Identifier.Chapter,
    val selectedVolumeId: Identifier.Volume? = null,
    val bookVolumeFlow: Flow<Result<BookVolumes, WebRequestError>>
)
