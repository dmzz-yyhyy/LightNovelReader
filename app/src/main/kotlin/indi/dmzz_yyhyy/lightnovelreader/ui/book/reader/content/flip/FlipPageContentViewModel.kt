package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip

import android.util.Log
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.snapshotFlow
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.content.ContentComponentRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentViewModel
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.math.roundToInt

class FlipPageContentViewModel(
    val bookRepository: BookRepository,
    val coroutineScope: CoroutineScope,
    val updateReadingProgress: (String, Float) -> Unit,
    val contentComponentRepository: ContentComponentRepository
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
                    snapshotFlow { pagerState.settledPage }.collect { page ->
                        val progress = if (pagerState.pageCount == 0) 0f
                        else ((page + 1) / pagerState.pageCount.toFloat()).coerceIn(0f, 1f)
                        uiState.readingProgress = progress
                        uiState.readingChapterContent?.onOk {
                            updateReadingProgress(it.id, progress)
                        }
                    }
                }
            }
        }
    }

    fun updatePagerState(pagerState: PagerState) {
        uiState.pagerState = pagerState
        if (pagerState.pageCount == 0) return
        val progressToRestore = when {
            notRecoveredProgress > 0f -> notRecoveredProgress.also { notRecoveredProgress = 0f }
            uiState.readingProgress > 0f -> uiState.readingProgress
            else -> return
        }
        val recovered = progressToRestore.coerceIn(0f, 1f)
        coroutineScope.launch {
            val target = ((pagerState.pageCount * recovered).roundToInt() - 1)
                .coerceIn(0, pagerState.pageCount - 1)
            uiState.pagerState.scrollToPage(target)
        }
    }

    override fun changeBookId(id: String) {
        uiState.bookId = id
    }

    override fun loadNextChapter() {
        uiState.readingChapterContent?.onOk {
            it.nextChapter?.let { id ->
                changeChapter(
                    id = id
                )
            }
        }
    }

    override fun loadLastChapter() {
        uiState.readingChapterContent?.onOk {
            it.lastChapter?.let { id ->
                changeChapter(
                    id = id
                )
            }
        }
    }

    override fun changeChapter(id: String) {
        if (id.isBlank()) {
            Log.e("FlipPageContentViewModel", "a id less than 0 was transferred")
            return
        }
        notRecoveredProgress = 0f
        uiState.readingProgress = 0f
        coroutineScope.launch {
            bookRepository.getChapterContentFlow(
                id,
                uiState.bookId,
                WebDataSourcePriority.High
            ).map { result ->
                result.map {
                    ChapterContentUiState(
                        id = it.id,
                        title = it.title,
                        content = contentComponentRepository.getContentDataFromJson(it.content).components,
                        lastChapter = it.lastChapter,
                        nextChapter = it.nextChapter
                    )
                }
            }.collect { result ->
                uiState.readingChapterId = id
                uiState.readingChapterContent = result
                result.onOk { content ->
                    bookRepository.updateUserReadingData(uiState.bookId) {
                        it.copy(
                            lastReadTime = LocalDateTime.now(),
                            lastReadChapterId = id,
                            lastReadChapterTitle = content.title
                        )
                    }
                    content.nextChapter?.let {
                        bookRepository.preloadChapterContent(
                            it,
                            uiState.bookId
                        )
                    }
                }
            }
        }
        coroutineScope.launch(Dispatchers.IO) {
            bookRepository.getUserReadingData(uiState.bookId).let {
                notRecoveredProgress = it.currentChapterReadingProgressMap[id] ?: 0f
            }
        }
    }
}