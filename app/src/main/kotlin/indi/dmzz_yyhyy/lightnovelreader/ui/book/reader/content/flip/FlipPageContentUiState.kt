package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.michaelbull.result.Result
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentUiState
import io.nightfish.lightnovelreader.api.error.WebRequestError

interface FlipPageContentUiState : ContentUiState {
    val locatedComponentHash: Int?
    val locatedFragmentHash: Int?
    val locateComponent: (Int?, Int?) -> Unit
    val pagerState: FlipPagerState
    val prevChapterContent: Result<ChapterContentUiState, WebRequestError>?
    val nextChapterContent: Result<ChapterContentUiState, WebRequestError>?
    val changeChapterAtBoundary: (String, Int) -> Unit
}

@Stable
class FlipPagerState {
    internal val measurementWindow = ChapterMeasurementWindow()
    var currentPage by mutableIntStateOf(0)
        private set
    var pageCount by mutableIntStateOf(0)
        private set
    private var requestedPage = 0
    var endReached by mutableStateOf(false)
        internal set
    var restoreTargetHash by mutableStateOf<Int?>(null)
        internal set
    var restoreTargetFragmentHash by mutableStateOf<Int?>(null)
        internal set
    var restoreInProgress by mutableStateOf(false)
        internal set
    var restoreToEnd by mutableStateOf(false)
        internal set
    var pageOffset by mutableFloatStateOf(0f)
        internal set
    var viewportWidth by mutableIntStateOf(0)
        internal set
    var pendingChapterDirection by mutableIntStateOf(0)
        internal set
    var pendingChapterId by mutableStateOf<String?>(null)
        internal set
    var isAnimating by mutableStateOf(false)
        internal set

    internal fun moveTo(page: Int) {
        requestedPage = page.coerceAtLeast(0)
        if (requestedPage < pageCount) currentPage = requestedPage
    }

    internal fun updatePageCount(count: Int) {
        pageCount = count.coerceAtLeast(0)
        currentPage = when {
            pageCount == 0 -> 0
            requestedPage < pageCount -> requestedPage
            else -> currentPage.coerceAtMost(pageCount - 1)
        }
    }

    internal fun reset(preserveChapterTransition: Boolean = false) {
        val transitionDirection = pendingChapterDirection
        val transitionChapterId = pendingChapterId
        val transitionOffset = pageOffset
        val hasChapterTransition = preserveChapterTransition &&
            transitionDirection != 0 &&
            transitionChapterId != null
        requestedPage = 0
        currentPage = 0
        pageCount = 0
        endReached = false
        restoreTargetHash = null
        restoreTargetFragmentHash = null
        restoreInProgress = false
        restoreToEnd = false
        pageOffset = if (hasChapterTransition) transitionOffset else 0f
        pendingChapterDirection = if (hasChapterTransition) transitionDirection else 0
        pendingChapterId = if (hasChapterTransition) transitionChapterId else null
        isAnimating = hasChapterTransition
    }
}

class MutableFlipPageContentUiState(
    override val loadNextChapter: () -> Unit,
    override val loadPrevChapter: () -> Unit,
    override val changeChapter: (String) -> Unit,
    override val changeChapterAtBoundary: (String, Int) -> Unit,
) : FlipPageContentUiState {
    override var locatedComponentHash by mutableStateOf<Int?>(null)
        private set
    override var locatedFragmentHash by mutableStateOf<Int?>(null)
        private set
    override val locateComponent: (Int?, Int?) -> Unit = { componentHash, fragmentHash ->
        locatedComponentHash = componentHash
        locatedFragmentHash = fragmentHash
    }
    override val pagerState = FlipPagerState()
    override var bookId by mutableStateOf("")
    override var readingChapterId: String? by mutableStateOf(null)
    override var readingChapterContent: Result<ChapterContentUiState, WebRequestError>? by mutableStateOf(
        null
    )
    override var prevChapterContent: Result<ChapterContentUiState, WebRequestError>? by mutableStateOf(null)
        internal set
    override var nextChapterContent: Result<ChapterContentUiState, WebRequestError>? by mutableStateOf(null)
        internal set
    override var readingProgress by mutableFloatStateOf(0f)
}
