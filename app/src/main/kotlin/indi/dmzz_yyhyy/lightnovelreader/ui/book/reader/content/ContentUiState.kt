package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import androidx.compose.runtime.Stable
import com.github.michaelbull.result.Result
import io.nightfish.lightnovelreader.api.error.WebRequestError

@Stable
interface ContentUiState {
    val bookId: String
    val readingChapterId: String?
    val readingChapterContent: Result<ChapterContentUiState, WebRequestError>?
    val readingProgress: Float
    val loadNextChapter: () -> Unit
    val loadPrevChapter: () -> Unit
    val changeChapter: (String) -> Unit
}