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
import io.nightfish.lightnovelreader.api.identifier.Identifier

@Stable
interface ReaderScreenUiState {
    val bookId: Identifier.Book?
    val chapterId: Identifier.Chapter?
    val userReadingData: UserReadingData?
    val bookVolumes: Result<BookVolumes, WebRequestError>?
    val readingProgress: Float?
    val preChapterContent: Result<ChapterContentUiState, WebRequestError>?
    val currentChapterContent: Result<ChapterContentUiState, WebRequestError>?
    val nextChapterContent: Result<ChapterContentUiState, WebRequestError>?
}

class MutableReaderScreenUiState(
) : ReaderScreenUiState {
    override var bookId: Identifier.Book? by mutableStateOf(null)
    override var chapterId: Identifier.Chapter? by mutableStateOf(null)
    override var userReadingData: UserReadingData? by mutableStateOf(null)
    override var bookVolumes: Result<BookVolumes, WebRequestError>? by mutableStateOf(null)
    override var readingProgress: Float? by mutableStateOf(null)
    override var preChapterContent: Result<ChapterContentUiState, WebRequestError>? by mutableStateOf(null)
    override var currentChapterContent: Result<ChapterContentUiState, WebRequestError>? by mutableStateOf(null)
    override var nextChapterContent: Result<ChapterContentUiState, WebRequestError>? by mutableStateOf(null)
}
