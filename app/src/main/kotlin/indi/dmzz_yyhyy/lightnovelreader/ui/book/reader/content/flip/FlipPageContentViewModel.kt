package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.flip

import android.util.Log
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.math.roundToInt

class FlipPageContentViewModel(
    val bookRepository: BookRepository,
    val coroutineScope: CoroutineScope,
    val updateReadingProgress: (String, Int, Float) -> Job?,
    val getReadingLocationHash: suspend (String) -> Int?,
    val contentComponentRepository: ContentComponentRepository
) : ContentViewModel {
    private var chapterJob: Job? = null
    private var prevChapterJob: Job? = null
    private var nextChapterJob: Job? = null

    private enum class ChapterEntry { SavedProgress, Start, End }
    override val uiState: MutableFlipPageContentUiState = MutableFlipPageContentUiState(
        loadPrevChapter = ::loadPrevChapter,
        loadNextChapter = ::loadNextChapter,
        changeChapter = ::changeChapter
    )

    init {
        coroutineScope.launch {
            snapshotFlow { uiState.locatedComponentHash to uiState.locatedFragmentHash }
                .map { (componentHash, locatedFragmentHash) ->
                    componentHash ?: return@map null
                    if (
                        uiState.pagerState.restoreTargetHash != null ||
                        uiState.pagerState.restoreInProgress ||
                        uiState.pagerState.restoreToEnd
                    ) return@map null
                    var currentContent: ChapterContentUiState? = null
                    uiState.readingChapterContent?.onOk { currentContent = it }
                    val content = currentContent ?: return@map null
                    val componentIndex = content.content.indexOfFirst { it.hashCode() == componentHash }
                    if (componentIndex < 0) return@map null
                    val fragmentHash = locatedFragmentHash ?: return@map null
                    val progress = if (content.content.size <= 1) 1f
                    else componentIndex.toFloat() / content.content.lastIndex
                    Triple(content.id, fragmentHash, progress)
                }
                .collectLatest { update ->
                    update ?: return@collectLatest
                    val (chapterId, fragmentHash, progress) = update
                    uiState.readingProgress = progress
                    // A fast swipe can generate pages more quickly than Room can persist them.
                    // Keep only the most recent component so an older queued write cannot win.
                    delay(250)
                    withContext(Dispatchers.IO) {
                        updateReadingProgress(chapterId, fragmentHash, progress)?.join()
                    }
                }
        }
    }

    override fun changeBookId(id: String) {
        uiState.bookId = id
    }

    override fun loadNextChapter() {
        uiState.readingChapterContent?.onOk {
            it.nextChapter?.takeIf(String::isNotBlank)?.let { id ->
                changeChapter(
                    id = id,
                    entry = ChapterEntry.Start,
                )
            }
        }
    }

    override fun loadPrevChapter() {
        uiState.readingChapterContent?.onOk {
            it.prevChapter?.takeIf(String::isNotBlank)?.let { id ->
                changeChapter(
                    id = id,
                    entry = ChapterEntry.End,
                )
            }
        }
    }

    override fun changeChapter(id: String) = changeChapter(id, ChapterEntry.SavedProgress)

    private fun changeChapter(id: String, entry: ChapterEntry) {
        if (id.isBlank()) {
            Log.e("FlipPageContentViewModel", "a id less than 0 was transferred")
            return
        }
        // A saved-progress request comes from navigation/the chapter selector and must never
        // inherit an unfinished horizontal chapter transition. Only the pager's explicit
        // previous/next requests are allowed to preserve the outgoing animation frame.
        val seamlessTransition = entry != ChapterEntry.SavedProgress &&
                uiState.pagerState.pendingChapterDirection != 0
        if (!seamlessTransition) {
            uiState.readingProgress = 0f
            uiState.locateComponent(null, null)
            uiState.pagerState.reset()
            uiState.pagerState.restoreInProgress = entry == ChapterEntry.SavedProgress
        }
        prevChapterJob?.cancel()
        nextChapterJob?.cancel()
        uiState.prevChapterContent = null
        uiState.nextChapterContent = null
        chapterJob?.cancel()
        chapterJob = coroutineScope.launch {
            val restoredProgress = if (entry == ChapterEntry.SavedProgress) {
                withContext(Dispatchers.IO) {
                    bookRepository.getUserReadingData(uiState.bookId)
                        .currentChapterReadingProgressMap[id]
                        ?.coerceIn(0f, 1f)
                        ?: 0f
                }
            } else 0f
            val restoredLocationHash = if (entry == ChapterEntry.SavedProgress) {
                withContext(Dispatchers.IO) { getReadingLocationHash(id) }
            } else null
            var firstResult = true
            var entryRestorationApplied = false
            bookRepository.getChapterContentFlow(
                id,
                uiState.bookId,
                WebDataSourcePriority.High
            ).map { result ->
                result.map {
                    ChapterContentUiState(
                        id = it.id,
                        title = it.title,
                        content = contentComponentRepository.getContentDataListFromJson(it.content),
                        prevChapter = it.prevChapter,
                        nextChapter = it.nextChapter
                    )
                }
            }.collect { result ->
                var loadedContent: ChapterContentUiState? = null
                result.onOk { loadedContent = it }
                val targetHash = loadedContent
                    ?.takeIf { it.content.isNotEmpty() }
                    ?.let { content ->
                        when (entry) {
                            ChapterEntry.Start, ChapterEntry.End -> null
                            ChapterEntry.SavedProgress -> if (restoredProgress > 0f) {
                                val targetIndex = (restoredProgress * content.content.lastIndex)
                                    .roundToInt()
                                    .coerceIn(0, content.content.lastIndex)
                                content.content[targetIndex].hashCode()
                            } else null
                        }
                    }
                val shouldApplyEntryRestoration = loadedContent != null && !entryRestorationApplied
                Snapshot.withMutableSnapshot {
                    if (firstResult) uiState.pagerState.reset()
                    uiState.readingChapterId = id
                    uiState.readingChapterContent = result
                    if (shouldApplyEntryRestoration) {
                        uiState.readingProgress = restoredProgress
                        uiState.pagerState.restoreTargetHash = targetHash
                        uiState.pagerState.restoreTargetFragmentHash = restoredLocationHash
                        uiState.pagerState.restoreInProgress = targetHash != null
                        uiState.pagerState.restoreToEnd = entry == ChapterEntry.End
                    }
                }
                firstResult = false
                if (shouldApplyEntryRestoration) entryRestorationApplied = true
                loadedContent?.let { content ->
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
                    prevChapterJob = preloadAdjacentChapter(content.prevChapter) {
                        uiState.prevChapterContent = it
                    }
                    nextChapterJob = preloadAdjacentChapter(content.nextChapter) {
                        uiState.nextChapterContent = it
                    }
                }
            }
        }
    }

    private fun preloadAdjacentChapter(
        chapterId: String?,
        onContent: (com.github.michaelbull.result.Result<ChapterContentUiState, io.nightfish.lightnovelreader.api.error.WebRequestError>) -> Unit,
    ): Job? {
        val id = chapterId ?: return null
        return coroutineScope.launch(Dispatchers.IO) {
            bookRepository.getChapterContentFlow(id, uiState.bookId).map { result ->
                result.map {
                    ChapterContentUiState(
                        id = it.id,
                        title = it.title,
                        content = contentComponentRepository.getContentDataListFromJson(it.content),
                        prevChapter = it.prevChapter,
                        nextChapter = it.nextChapter,
                    )
                }
            }.collect(onContent)
        }
    }
}
