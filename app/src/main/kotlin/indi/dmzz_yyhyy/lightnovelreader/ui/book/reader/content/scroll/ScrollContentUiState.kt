package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntSize
import com.github.michaelbull.result.Result
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentUiState
import io.nightfish.lightnovelreader.api.error.WebRequestError

interface ScrollContentUiState: ContentUiState {
    val lazyListState: LazyListState
    val contentList: List<Pair<String, Result<ChapterContentUiState, WebRequestError>>?>
    val setLazyColumnSize: (IntSize) -> Unit
    val writeProgressRightNow: () -> Unit
    override val readingChapterContent: Result<ChapterContentUiState, WebRequestError>?
        get() = contentList.firstOrNull { it?.first == readingChapterId }?.second
}

class MutableScrollContentUiSate(
    override val loadNextChapter: () -> Unit,
    override val loadLastChapter: () -> Unit,
    override val changeChapter: (String) -> Unit,
    override val setLazyColumnSize: (IntSize) -> Unit,
    override val writeProgressRightNow: () -> Unit
) : ScrollContentUiState {
    override var bookId by mutableStateOf("")
    override var readingProgress by mutableFloatStateOf(0f)
    override var lazyListState: LazyListState by mutableStateOf(LazyListState())
    override var readingChapterId: String? by mutableStateOf(null)
    override val contentList = mutableStateListOf<Pair<String, Result<ChapterContentUiState, WebRequestError>>?>(null, null, null)
}