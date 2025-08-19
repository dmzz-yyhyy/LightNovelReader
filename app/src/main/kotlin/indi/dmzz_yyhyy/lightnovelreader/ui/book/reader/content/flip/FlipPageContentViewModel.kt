package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip

import android.util.Log
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.snapshotFlow
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class FlipPageContentViewModel(
    val bookRepository: BookRepository,
    val coroutineScope: CoroutineScope,
    val updateReadingProgress: (Float) -> Unit
) : ContentViewModel {
    private var notRecoveredProgress = 0f
    private var collectProgressJob: Job? = null
    override val uiState: MutableFlipPageContentUiState = MutableFlipPageContentUiState(
        loadLastChapter = ::loadLastChapter,
        loadNextChapter = ::loadNextChapter,
        changeChapter = ::changeChapter,
        updatePageState = ::updatePagerState
    )

    init {
        coroutineScope.launch(Dispatchers.IO) {
            snapshotFlow { uiState.pagerState }.collect { pagerState ->
                collectProgressJob?.cancel()
                collectProgressJob = coroutineScope.launch(Dispatchers.IO) {
                    snapshotFlow { pagerState.settledPage }.collect {
                        val readingProgress = (it + 1) / pagerState.pageCount.toFloat()
                        if (readingProgress in 0.0..1.0) {
                            uiState.readingProgress = (it + 1) / pagerState.pageCount.toFloat()
                            updateReadingProgress(uiState.readingProgress)
                        }
                    }
                }
            }
        }
    }

    fun updatePagerState(pagerState: PagerState) {
        uiState.pagerState = pagerState
        if (pagerState.pageCount == 0) return
        if (notRecoveredProgress <= 0) return
        val recoveredProgress = this.notRecoveredProgress
        notRecoveredProgress = 0f
        coroutineScope.launch {
            uiState.pagerState.scrollToPage((pagerState.pageCount * recoveredProgress).toInt() - 1)
        }
    }

    override fun changeBookId(id: Int) {
        uiState.bookId = id
    }

    override fun loadNextChapter() {
        if (!uiState.readingChapterContent.hasNextChapter()) return
        changeChapter(
            id = uiState.readingChapterContent.nextChapter
        )
    }

    override fun loadLastChapter() {
        if (!uiState.readingChapterContent.hasLastChapter()) return
        changeChapter(
            id = uiState.readingChapterContent.lastChapter
        )
    }

    override fun changeChapter(id: Int) {
        if (id < 0) {
            Log.e("FlipPageContentViewModel", "a id less than 0 was transferred")
            return
        }
        notRecoveredProgress = 0f
        uiState.readingProgress = 0f
        uiState.readingChapterContent = bookRepository.getStateChapterContent(id, uiState.bookId, coroutineScope)
        uiState.readingChapterContent
        coroutineScope.launch(Dispatchers.IO) {
            snapshotFlow { uiState.readingChapterContent.title }.collect { title ->
                bookRepository.updateUserReadingData(uiState.bookId) {
                    it.apply {
                        lastReadChapterProgress =
                            if (it.lastReadChapterId == id) it.lastReadChapterProgress else 0f
                        lastReadTime = LocalDateTime.now()
                        lastReadChapterId = id
                        lastReadChapterTitle = title
                    }
                }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            snapshotFlow { uiState.readingChapterContent.nextChapter }.collect {
                if (uiState.readingChapterContent.hasNextChapter()) {
                    bookRepository.getChapterContentFlow(
                        chapterId = it,
                        bookId = uiState.bookId,
                        coroutineScope
                    )
                }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            bookRepository.getUserReadingData(uiState.bookId).let {
                if (it.lastReadChapterId == uiState.readingChapterContent.id)
                    notRecoveredProgress = it.lastReadChapterProgress
            }
        }
    }
}