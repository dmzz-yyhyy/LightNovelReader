package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.michaelbull.result.Result
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentUiState
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.UserReadingData
import io.nightfish.lightnovelreader.api.error.WebRequestError

@Stable
interface ReaderScreenUiState {
    val bookId: String?
    val chapterId: String?
    val userReadingData: UserReadingData?
    val bookVolumes: Result<BookVolumes, WebRequestError>?
    val readingProgress: Float?
    val preChapterContent: Result<ChapterContentUiState, WebRequestError>?
    val currentChapterContent: Result<ChapterContentUiState, WebRequestError>?
    val nextChapterContent: Result<ChapterContentUiState, WebRequestError>?
}

class MutableReaderScreenUiState(
) : ReaderScreenUiState {
    override var bookId: String? by mutableStateOf(null)
    override var chapterId: String? by mutableStateOf(null)
    override var userReadingData: UserReadingData? by mutableStateOf(null)
    override var bookVolumes: Result<BookVolumes, WebRequestError>? by mutableStateOf(null)
    override var readingProgress: Float? by mutableStateOf(null)
    override var preChapterContent: Result<ChapterContentUiState, WebRequestError>? by mutableStateOf(null)
    override var currentChapterContent: Result<ChapterContentUiState, WebRequestError>? by mutableStateOf(null)
    override var nextChapterContent: Result<ChapterContentUiState, WebRequestError>? by mutableStateOf(null)
}