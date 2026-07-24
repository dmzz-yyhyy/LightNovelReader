package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ScrollContentViewModel(
    val bookRepository: BookRepository,
    val coroutineScope: CoroutineScope,
    val settingState: SettingState,
    val contentComponentRepository: ContentComponentRepository,
    val updateReadingProgress: (String, Float) -> Unit
) : ContentViewModel {
    private var progressScrollLoadJob: Job? = null
    private var lazyColumnSize = IntSize(0, 0)
    private var lastWriteReadingProgress = 0L
    private var collectPrevChapterJob: Job? = null
    private var collectCurrentChapterJob: Job? = null
    private var collectNextChapterJob: Job? = null

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
        coroutineScope.launch {
            settingState.isUsingContinuousScrollingUserData.getFlowWithDefault(true).collect {
                if (it) {
                    progressScrollLoad()
                    val hasAdjacentChapters = uiState.contentList.getOrNull(0) != null || uiState.contentList.getOrNull(2) != null
                    if (!hasAdjacentChapters) {
                        coroutineScope.launch(Dispatchers.Main) {
                            uiState.readingChapterId?.let { id -> changeChapter(id) }
                        }
                    }
                } else {
                    progressScrollLoadJob?.cancel()
                    val hasAdjacentChapters = uiState.contentList.getOrNull(0) != null || uiState.contentList.getOrNull(2) != null
                    if (hasAdjacentChapters) {
                        coroutineScope.launch(Dispatchers.Main) {
                            uiState.readingChapterId?.let { id -> changeChapter(id) }
                        }
                    }
                }
            }
        }
        coroutineScope.launch(Dispatchers.Main) {
            snapshotFlow { uiState.lazyListState.firstVisibleItemScrollOffset }
                .throttleLatest(120L)
                .collect {
                    val layoutInfo = uiState.lazyListState.layoutInfo
                    val chapterId = uiState.readingChapterId ?: return@collect
                    val item = layoutInfo.visibleItemsInfo.firstOrNull { it.key == chapterId } ?: return@collect

                    val newProgress = calculateReadingProgress(item.offset, item.size)
                    if (newProgress == uiState.readingProgress) return@collect
                    uiState.readingProgress = newProgress

                    val now = System.currentTimeMillis()
                    val scrolling = uiState.lazyListState.isScrollInProgress

                    if (scrolling && now - lastWriteReadingProgress < 2500 && newProgress < 1f) return@collect
                    lastWriteReadingProgress = now

                    coroutineScope.launch(Dispatchers.IO) { updateReadingProgress(chapterId, newProgress) }
                }
        }

        coroutineScope.launch(Dispatchers.Main) {
            snapshotFlow { uiState.lazyListState.isScrollInProgress }
                .distinctUntilChanged()
                .collect { scrolling ->
                    if (!scrolling) {
                        val layoutInfo = uiState.lazyListState.layoutInfo
                        val chapterId = uiState.readingChapterId ?: return@collect
                        val item = layoutInfo.visibleItemsInfo.firstOrNull { it.key == chapterId } ?: return@collect

                        val finalProgress = calculateReadingProgress(item.offset, item.size)

                        if (uiState.readingProgress != finalProgress) {
                            uiState.readingProgress = finalProgress
                        }
                        coroutineScope.launch(Dispatchers.IO) { updateReadingProgress(chapterId, uiState.readingProgress) }
                        lastWriteReadingProgress = System.currentTimeMillis()
                    }
                }
        }
    }


    private fun writeProgressRightNow() {
        updateReadingProgress(uiState.readingChapterId ?: return, uiState.readingProgress)
    }

    private fun progressScrollLoad() {
        progressScrollLoadJob?.cancel()
        progressScrollLoadJob = coroutineScope.launch {
            snapshotFlow { uiState.lazyListState.layoutInfo.visibleItemsInfo.getOrNull(0) }.collect { itemInfo ->
                uiState.readingChapterContent?.onOk { readingChapterContent ->
                    if (
                        itemInfo != null &&
                        itemInfo.key == readingChapterContent.prevChapter &&
                        lazyColumnSize.height != 0 &&
                        itemInfo.offset <= -lazyColumnSize.height &&
                        readingChapterContent.hasPrevChapter()
                    ) {
                        collectNextChapterJob?.cancel()
                        collectCurrentChapterJob?.cancel()
                        collectPrevChapterJob?.cancel()
                        val nextChapter = uiState.contentList[1]
                        val currentChapter = uiState.contentList[0]
                        val currentChapterId = readingChapterContent.prevChapter
                        val currentChapterContent = currentChapter?.second?.get()
                        resetContentList()
                        uiState.contentList[2] = nextChapter
                        uiState.contentList[1] = currentChapter
                        collectNextChapterJob = collectChapter(2, readingChapterContent.id)
                        collectCurrentChapterJob = collectChapter(1, currentChapterId) { chapterContent ->
                            collectPrevChapterJob?.cancel()
                            collectPrevChapterJob = collectAdjacentChapter(
                                index = 0,
                                chapterId = chapterContent.prevChapter,
                                currentChapterId = chapterContent.id,
                                occupiedChapterIds = setOf(readingChapterContent.id)
                            )
                            updateLastReadChapter(chapterContent.id, chapterContent.title)
                        }
                        uiState.readingChapterId = currentChapterId
                        currentChapterContent?.let {
                            updateLastReadChapter(it.id, it.title)
                        }
                    }
                    if (
                        itemInfo != null &&
                        itemInfo.key == readingChapterContent.nextChapter &&
                        readingChapterContent.hasNextChapter()
                    ) {
                        collectNextChapterJob?.cancel()
                        collectCurrentChapterJob?.cancel()
                        collectPrevChapterJob?.cancel()
                        val prevChapter = uiState.contentList[1]
                        val currentChapter = uiState.contentList[2]
                        val currentChapterId = readingChapterContent.nextChapter
                        val currentChapterContent = currentChapter?.second?.get()
                        resetContentList()
                        uiState.contentList[0] = prevChapter
                        uiState.contentList[1] = currentChapter
                        collectPrevChapterJob = collectChapter(0, readingChapterContent.id)
                        collectCurrentChapterJob = collectChapter(1, currentChapterId) { chapterContent ->
                            collectNextChapterJob?.cancel()
                            collectNextChapterJob = collectAdjacentChapter(
                                index = 2,
                                chapterId = chapterContent.nextChapter,
                                currentChapterId = chapterContent.id,
                                occupiedChapterIds = setOf(readingChapterContent.id)
                            )
                            updateLastReadChapter(chapterContent.id, chapterContent.title)
                        }
                        uiState.readingChapterId = currentChapterId
                        currentChapterContent?.let {
                            updateLastReadChapter(it.id, it.title)
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
        resetContentList()
        uiState.readingChapterId = id
        uiState.readingProgress = 0f
        uiState.lazyListState = LazyListState()
        coroutineScope.launch (Dispatchers.IO) {
            val isUsingContinuousScrolling = settingState.isUsingContinuousScrollingUserData.getOrDefault(true)
            if (isUsingContinuousScrolling) changeChapterWithContinuousScrolling(id)
            else changeChapterWithoutContinuousScrolling(id)
        }
    }

    private fun changeChapterWithoutContinuousScrolling(id: String) {
        collectCurrentChapterJob?.cancel()
        collectCurrentChapterJob = coroutineScope.launch(Dispatchers.IO) {
            bookRepository.getChapterContentFlow(id, uiState.bookId).collect { result ->
                uiState.contentList[1] = id to result.map {
                    ChapterContentUiState(
                        id = it.id,
                        title = it.title,
                        content = contentComponentRepository.getContentDataFromJson(it.content).components,
                        prevChapter = it.prevChapter,
                        nextChapter = it.nextChapter
                    )
                }
                result.onOk { chapterContent ->
                    bookRepository.updateUserReadingData(uiState.bookId) { userReadingData ->
                        uiState.readingProgress = userReadingData.currentChapterReadingProgressMap[id] ?: 0f
                        userReadingData.copy(
                            lastReadTime = LocalDateTime.now(),
                            lastReadChapterId = id,
                            lastReadChapterTitle = chapterContent.title,
                        )
                    }
                    chapterContent.nextChapter?.let {
                        bookRepository.preloadChapterContent(
                            it,
                            uiState.bookId
                        )
                    }
                }
            }
        }
    }

    private fun changeChapterWithContinuousScrolling(id: String) {
        collectCurrentChapterJob?.cancel()
        collectCurrentChapterJob = coroutineScope.launch(Dispatchers.IO) {
            bookRepository.getChapterContentFlow(id, uiState.bookId).collect { result ->
                uiState.contentList[1] = id to result.map {
                    ChapterContentUiState(
                        id = it.id,
                        title = it.title,
                        content = contentComponentRepository.getContentDataFromJson(it.content).components,
                        prevChapter = it.prevChapter,
                        nextChapter = it.nextChapter
                    )
                }
                result.onOk { chapterContent ->
                    bookRepository.updateUserReadingData(uiState.bookId) { userReadingData ->
                        uiState.readingProgress = userReadingData.currentChapterReadingProgressMap[id] ?: 0f
                        userReadingData.copy(
                            lastReadTime = LocalDateTime.now(),
                            lastReadChapterId = id,
                            lastReadChapterTitle = chapterContent.title,
                        )
                    }
                    chapterContent.nextChapter?.let {
                        bookRepository.preloadChapterContent(
                            it,
                            uiState.bookId
                        )
                    }

                    collectPrevChapterJob?.cancel()
                    collectPrevChapterJob = collectAdjacentChapter(
                        index = 0,
                        chapterId = chapterContent.prevChapter,
                        currentChapterId = chapterContent.id,
                        occupiedChapterIds = setOfNotNull(chapterContent.nextChapter)
                    )
                    collectNextChapterJob?.cancel()
                    collectNextChapterJob = collectAdjacentChapter(
                        index = 2,
                        chapterId = chapterContent.nextChapter,
                        currentChapterId = chapterContent.id,
                        occupiedChapterIds = setOfNotNull(chapterContent.prevChapter)
                    )
                }
            }
        }
    }

    private fun collectChapter(
        index: Int,
        chapterId: String,
        onLoaded: suspend (ChapterContentUiState) -> Unit = {}
    ) = coroutineScope.launch {
            bookRepository.getChapterContentFlow(chapterId, uiState.bookId)
                .collect { content ->
                    var loadedContent: ChapterContentUiState? = null
                    uiState.contentList[index] = chapterId to content.map {
                        ChapterContentUiState(
                            id = it.id,
                            title = it.title,
                            content = contentComponentRepository.getContentDataFromJson(it.content).components,
                            prevChapter = it.prevChapter,
                            nextChapter = it.nextChapter
                        ).also { chapterContentUiState ->
                            loadedContent = chapterContentUiState
                        }
                    }
                    loadedContent?.let { onLoaded(it) }
                }
        }

    private fun collectAdjacentChapter(
        index: Int,
        chapterId: String?,
        currentChapterId: String,
        occupiedChapterIds: Set<String> = emptySet()
    ): Job? {
        val adjacentChapterId = chapterId
            ?.takeIf { it != currentChapterId }
            ?.takeIf { it !in occupiedChapterIds }
            ?: run {
                uiState.contentList[index] = null
                return null
            }
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
        ((-itemOffset + lazyColumnSize.height).toFloat() / itemSize.coerceAtLeast(1)).coerceIn(0f, 1f)
}
