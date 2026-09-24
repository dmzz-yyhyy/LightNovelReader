package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll

import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.unit.IntSize
import com.github.michaelbull.result.get
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.content.ContentComponentRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentViewModel
import indi.dmzz_yyhyy.lightnovelreader.utils.throttleLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class ScrollContentViewModel(
    val bookRepository: BookRepository,
    val coroutineScope: CoroutineScope,
    val settingState: SettingState,
    val contentComponentRepository: ContentComponentRepository,
    val updateReadingProgress: (String, Int, Float) -> Job?
) : ContentViewModel {
    private var progressScrollLoadJob: Job? = null
    private var lazyColumnSize = IntSize(0, 0)
    private var lastWriteReadingProgress = 0L
    private var collectPrevChapterJob: Job? = null
    private var collectCurrentChapterJob: Job? = null
    private var collectNextChapterJob: Job? = null
    private val adjacentChapterRequests = arrayOfNulls<String>(3)

    override val uiState: MutableScrollContentUiSate = MutableScrollContentUiSate(
        loadPrevChapter = ::loadPrevChapter,
        loadNextChapter = ::loadNextChapter,
        changeChapter = ::changeChapter,
        setLazyColumnSize = {
            lazyColumnSize = it
        },
        writeProgressRightNow = ::writeProgressRightNow
    )

    init {
        progressScrollLoad()
        coroutineScope.launch(Dispatchers.Main) {
            snapshotFlow {
                uiState.lazyListState.firstVisibleItemIndex to
                        uiState.lazyListState.firstVisibleItemScrollOffset
            }
                .throttleLatest(120L)
                .collect {
                    if (uiState.isRestoringProgress) return@collect
                    val layoutInfo = uiState.lazyListState.layoutInfo
                    val chapterId = uiState.readingChapterId ?: return@collect
                    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.key == chapterId }
                        ?: return@collect

                    val newProgress = calculateReadingProgress(item.offset, item.size)
                    if (newProgress == uiState.readingProgress) return@collect
                    uiState.readingProgress = newProgress

                    val now = System.currentTimeMillis()
                    val scrolling = uiState.lazyListState.isScrollInProgress

                    if (scrolling && now - lastWriteReadingProgress < 2500 && newProgress < 1f) return@collect
                    lastWriteReadingProgress = now

                    coroutineScope.launch(Dispatchers.IO) {
                        updateReadingProgress(
                            chapterId,
                            chapterId.hashCode(),
                            newProgress
                        )
                    }
                }
        }

        coroutineScope.launch(Dispatchers.Main) {
            snapshotFlow { uiState.lazyListState.isScrollInProgress }
                .distinctUntilChanged()
                .collect { scrolling ->
                    if (!scrolling) {
                        if (uiState.isRestoringProgress) return@collect
                        val layoutInfo = uiState.lazyListState.layoutInfo
                        val chapterId = uiState.readingChapterId ?: return@collect
                        val item = layoutInfo.visibleItemsInfo.firstOrNull { it.key == chapterId }
                            ?: return@collect

                        val finalProgress = calculateReadingProgress(item.offset, item.size)

                        if (uiState.readingProgress != finalProgress) {
                            uiState.readingProgress = finalProgress
                        }
                        coroutineScope.launch(Dispatchers.IO) {
                            updateReadingProgress(
                                chapterId,
                                chapterId.hashCode(),
                                uiState.readingProgress
                            )
                        }
                        lastWriteReadingProgress = System.currentTimeMillis()
                    }
                }
        }
    }


    private fun writeProgressRightNow() {
        if (uiState.isRestoringProgress) return
        val chapterId = uiState.readingChapterId ?: return
        updateReadingProgress(chapterId, chapterId.hashCode(), uiState.readingProgress)
    }

    private fun progressScrollLoad() {
        progressScrollLoadJob?.cancel()
        progressScrollLoadJob = coroutineScope.launch {
            snapshotFlow { uiState.lazyListState.layoutInfo.visibleItemsInfo }.collect { visibleItems ->
                if (uiState.isRestoringProgress) return@collect
                val layoutInfo = uiState.lazyListState.layoutInfo
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                val itemInfo = visibleItems.firstOrNull {
                    viewportCenter in it.offset until (it.offset + it.size)
                } ?: return@collect
                uiState.readingChapterContent?.onOk { readingChapterContent ->
                    if (
                        itemInfo.key == readingChapterContent.prevChapter &&
                        readingChapterContent.hasPrevChapter() &&
                        uiState.lazyListState.lastScrolledBackward
                    ) {
                        collectNextChapterJob?.cancel()
                        collectCurrentChapterJob?.cancel()
                        collectPrevChapterJob?.cancel()
                        val nextChapter = uiState.contentList[1]
                        val currentChapter = uiState.contentList[0] ?: return@onOk
                        val currentChapterId = currentChapter.first
                        val currentChapterContent = currentChapter.second.get()
                        val anchorIndex = uiState.lazyListState.firstVisibleItemIndex
                        val anchorOffset = uiState.lazyListState.firstVisibleItemScrollOffset
                        Snapshot.withMutableSnapshot {
                            uiState.contentList[0] = null
                            uiState.contentList[2] = nextChapter
                            uiState.contentList[1] = currentChapter
                            uiState.readingChapterId = currentChapterId
                            uiState.lazyListState.requestScrollToItem(
                                index = (anchorIndex + 1).coerceAtMost(2),
                                scrollOffset = anchorOffset,
                            )
                        }
                        currentChapterContent?.let { chapterContent ->
                            collectPrevChapterJob = collectAdjacentChapter(
                                index = 0,
                                chapterId = chapterContent.prevChapter,
                                currentChapterId = chapterContent.id,
                                occupiedChapterIds = setOfNotNull(nextChapter?.first)
                            )
                            updateLastReadChapter(chapterContent.id, chapterContent.title)
                        }
                    }
                    if (
                        itemInfo.key == readingChapterContent.nextChapter &&
                        readingChapterContent.hasNextChapter() &&
                        uiState.lazyListState.lastScrolledForward
                    ) {
                        collectNextChapterJob?.cancel()
                        collectCurrentChapterJob?.cancel()
                        collectPrevChapterJob?.cancel()
                        val prevChapter = uiState.contentList[1]
                        val currentChapter = uiState.contentList[2] ?: return@onOk
                        val currentChapterId = currentChapter.first
                        val currentChapterContent = currentChapter.second.get()
                        val anchorIndex = uiState.lazyListState.firstVisibleItemIndex
                        val anchorOffset = uiState.lazyListState.firstVisibleItemScrollOffset
                        Snapshot.withMutableSnapshot {
                            uiState.contentList[0] = prevChapter
                            uiState.contentList[1] = currentChapter
                            uiState.contentList[2] = null
                            uiState.readingChapterId = currentChapterId
                            uiState.lazyListState.requestScrollToItem(
                                index = (anchorIndex - 1).coerceAtLeast(0),
                                scrollOffset = anchorOffset,
                            )
                        }
                        currentChapterContent?.let { chapterContent ->
                            collectNextChapterJob = collectAdjacentChapter(
                                index = 2,
                                chapterId = chapterContent.nextChapter,
                                currentChapterId = chapterContent.id,
                                occupiedChapterIds = setOfNotNull(prevChapter?.first)
                            )
                            updateLastReadChapter(chapterContent.id, chapterContent.title)
                        }
                    }
                }
            }
        }
    }

    override fun changeBookId(id: String) {
        uiState.bookId = id
    }

    override fun loadNextChapter() {
        uiState.readingChapterContent?.onOk { readingChapterContent ->
            if (!readingChapterContent.hasNextChapter()) return
            coroutineScope.launch {
                changeChapter(
                    id = readingChapterContent.nextChapter ?: return@launch
                )
            }
        }
    }

    override fun loadPrevChapter() {
        uiState.readingChapterContent?.onOk { readingChapterContent ->
            if (!readingChapterContent.hasPrevChapter()) return
            coroutineScope.launch {
                changeChapter(
                    id = readingChapterContent.prevChapter ?: return@launch
                )
            }
        }
    }

    private fun resetContentList() {
        uiState.contentList.clear()
        uiState.contentList.add(null)
        uiState.contentList.add(null)
        uiState.contentList.add(null)
    }

    override fun changeChapter(id: String) {
        collectPrevChapterJob?.cancel()
        collectCurrentChapterJob?.cancel()
        collectNextChapterJob?.cancel()
        adjacentChapterRequests.fill(null)
        resetContentList()
        uiState.readingChapterId = id
        uiState.readingProgress = 0f
        uiState.isRestoringProgress = true
        uiState.lazyListState = preloadedChapterListState()
        changeChapterWithContinuousScrolling(id)
    }

    private fun changeChapterWithContinuousScrolling(id: String) {
        collectCurrentChapterJob?.cancel()
        collectCurrentChapterJob = coroutineScope.launch(Dispatchers.IO) {
            val restoredProgress = bookRepository
                .getUserReadingData(uiState.bookId)
                .currentChapterReadingProgressMap[id]
                ?: 0f
            withContext(Dispatchers.Main) {
                uiState.readingProgress = restoredProgress.coerceIn(0f, 1f)
            }
            bookRepository.getChapterContentFlow(id, uiState.bookId).collect { result ->
                uiState.contentList[1] = id to result.map {
                    ChapterContentUiState(
                        id = it.id,
                        title = it.title,
                        content = contentComponentRepository.getContentDataListFromJson(it.content),
                        prevChapter = it.prevChapter,
                        nextChapter = it.nextChapter
                    )
                }
                result.onOk { chapterContent ->
                    // Populate both sides first. Waiting for metadata writes or another network
                    // preload here leaves the seamless-scroll window empty at chapter boundaries.
                    collectPrevChapterJob = collectAdjacentChapter(
                        index = 0,
                        chapterId = chapterContent.prevChapter,
                        currentChapterId = chapterContent.id,
                        occupiedChapterIds = setOfNotNull(chapterContent.nextChapter)
                    )
                    collectNextChapterJob = collectAdjacentChapter(
                        index = 2,
                        chapterId = chapterContent.nextChapter,
                        currentChapterId = chapterContent.id,
                        occupiedChapterIds = setOfNotNull(chapterContent.prevChapter)
                    )
                    coroutineScope.launch(Dispatchers.IO) {
                        bookRepository.updateUserReadingData(uiState.bookId) { userReadingData ->
                            userReadingData.copy(
                                lastReadTime = LocalDateTime.now(),
                                lastReadChapterId = id,
                                lastReadChapterTitle = chapterContent.title,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun collectChapter(
        index: Int,
        chapterId: String,
        onLoaded: suspend (ChapterContentUiState) -> Unit = {}
    ) = coroutineScope.launch(Dispatchers.IO) {
        bookRepository.getChapterContentFlow(chapterId, uiState.bookId)
            .collect { content ->
                var loadedContent: ChapterContentUiState? = null
                val mappedContent = content.map {
                    ChapterContentUiState(
                        id = it.id,
                        title = it.title,
                        content = contentComponentRepository.getContentDataListFromJson(it.content),
                        prevChapter = it.prevChapter,
                        nextChapter = it.nextChapter
                    ).also { chapterContentUiState ->
                        loadedContent = chapterContentUiState
                    }
                }
                if (index != 1 && !uiState.isRestoringProgress) {
                    snapshotFlow { uiState.lazyListState.isScrollInProgress }
                        .first { scrolling -> !scrolling }
                }
                withContext(Dispatchers.Main) {
                    uiState.contentList[index] = chapterId to mappedContent
                    loadedContent?.let { onLoaded(it) }
                }
            }
    }

    private fun collectAdjacentChapter(
        index: Int,
        chapterId: String?,
        currentChapterId: String,
        occupiedChapterIds: Set<String> = emptySet()
    ): Job? {
        val existingJob = when (index) {
            0 -> collectPrevChapterJob
            2 -> collectNextChapterJob
            else -> null
        }
        val adjacentChapterId = chapterId
            ?.takeIf { it != currentChapterId }
            ?.takeIf { it !in occupiedChapterIds }
            ?: run {
                existingJob?.cancel()
                adjacentChapterRequests[index] = null
                uiState.contentList[index] = null
                return null
            }
        if (
            adjacentChapterRequests[index] == adjacentChapterId &&
            (
                existingJob?.isActive == true ||
                    uiState.contentList.getOrNull(index)?.let { loaded ->
                        loaded.first == adjacentChapterId && loaded.second.get() != null
                    } == true
            )
        ) {
            return existingJob
        }
        existingJob?.cancel()
        adjacentChapterRequests[index] = adjacentChapterId
        return collectChapter(index, adjacentChapterId)
    }

    private suspend fun updateLastReadChapter(chapterId: String, chapterTitle: String?) {
        bookRepository.updateUserReadingData(uiState.bookId) {
            it.copy(
                lastReadTime = LocalDateTime.now(),
                lastReadChapterId = chapterId,
                lastReadChapterTitle = chapterTitle ?: it.lastReadChapterTitle
            )
        }
    }

    private fun calculateReadingProgress(itemOffset: Int, itemSize: Int): Float =
        ((-itemOffset).toFloat() /
                // The active chapter changes when its end crosses the viewport center.
                // Using a full viewport here made the last half-screen clamp to 100%, so
                // restoring that value jumped backwards by roughly half a screen.
                (itemSize - lazyColumnSize.height / 2).coerceAtLeast(1)).coerceIn(
            0f,
            1f
        )
}
