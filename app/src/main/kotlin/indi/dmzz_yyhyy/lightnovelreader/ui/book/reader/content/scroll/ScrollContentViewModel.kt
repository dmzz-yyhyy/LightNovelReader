package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.IntSize
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
    private var collectLastChapterJob: Job? = null
    private var collectCurrentChapterJob: Job? = null
    private var collectNextChapterJob: Job? = null

    override val uiState: MutableScrollContentUiSate = MutableScrollContentUiSate(
        loadLastChapter = ::loadLastChapter,
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
                    if (uiState.contentList.size == 1) {
                        coroutineScope.launch(Dispatchers.Main) {
                            uiState.readingChapterId?.let { id -> changeChapter(id) }
                        }
                    }
                } else {
                    progressScrollLoadJob?.cancel()
                    if (uiState.contentList.size > 1) {
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

                    val newProgress = 1f.coerceAtMost((-item.offset + lazyColumnSize.height).toFloat() / item.size)
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

                        val finalProgress = 1f.coerceAtMost(
                            (-item.offset + lazyColumnSize.height).toFloat() / item.size.coerceAtLeast(1)
                        )

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
                        itemInfo.key == readingChapterContent.lastChapter &&
                        lazyColumnSize.height != 0 &&
                        itemInfo.offset <= -lazyColumnSize.height &&
                        readingChapterContent.hasPrevChapter()
                    ) {
                        collectNextChapterJob?.cancel()
                        collectCurrentChapterJob?.cancel()
                        collectLastChapterJob?.cancel()
                        val chapter1 = uiState.contentList[1]
                        val chapter0 = uiState.contentList[0]
                        resetContentList()
                        uiState.contentList[2] = chapter1
                        uiState.contentList[1] = chapter0
                        collectNextChapterJob = collectChapter(2, readingChapterContent.id)
                        collectCurrentChapterJob = collectChapter(1, readingChapterContent.lastChapter)
                        uiState.readingChapterId = readingChapterContent.lastChapter
                        bookRepository.updateUserReadingData(uiState.bookId) {
                            it.copy(
                                lastReadTime = LocalDateTime.now(),
                                lastReadChapterId = readingChapterContent.id,
                                lastReadChapterTitle = readingChapterContent.title
                            )
                        }
                        if (readingChapterContent.hasPrevChapter())
                            collectLastChapterJob = collectChapter(0, readingChapterContent.lastChapter)
                    }
                    if (
                        itemInfo != null &&
                        itemInfo.key == readingChapterContent.nextChapter &&
                        readingChapterContent.hasNextChapter()
                    ) {
                        collectNextChapterJob?.cancel()
                        collectCurrentChapterJob?.cancel()
                        collectLastChapterJob?.cancel()
                        val chapter1 = uiState.contentList[1]
                        val chapter2 = uiState.contentList[2]
                        resetContentList()
                        uiState.contentList[0] = chapter1
                        uiState.contentList[1] = chapter2
                        collectLastChapterJob = collectChapter(0, readingChapterContent.id)
                        collectCurrentChapterJob = collectChapter(1, readingChapterContent.nextChapter)
                        uiState.readingChapterId = readingChapterContent.nextChapter
                        bookRepository.updateUserReadingData(uiState.bookId) {
                            it.copy(
                                lastReadTime = LocalDateTime.now(),
                                lastReadChapterId = readingChapterContent.id,
                                lastReadChapterTitle = readingChapterContent.title
                            )
                        }
                        if (readingChapterContent.hasNextChapter())
                            collectNextChapterJob = collectChapter(2, readingChapterContent.nextChapter)
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

    override fun loadLastChapter() {
        uiState.readingChapterContent?.onOk { readingChapterContent ->
            if (!readingChapterContent.hasPrevChapter()) return
            coroutineScope.launch {
                changeChapter(
                    id = readingChapterContent.lastChapter ?: return@launch
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
                        lastChapter = it.lastChapter,
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
                        lastChapter = it.lastChapter,
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

                    chapterContent.lastChapter?.let {
                        collectLastChapterJob?.cancel()
                        collectLastChapterJob = collectChapter(0, it)
                    }
                    chapterContent.nextChapter?.let {
                        collectNextChapterJob?.cancel()
                        collectNextChapterJob = collectChapter(2, it)
                    }
                }
            }
        }
    }

    private fun collectChapter(index: Int, chapterId: String) = coroutineScope.launch {
            bookRepository.getChapterContentFlow(chapterId, uiState.bookId)
                .collect { content ->
                    uiState.contentList[index] = chapterId to content.map {
                        ChapterContentUiState(
                            id = it.id,
                            title = it.title,
                            content = contentComponentRepository.getContentDataFromJson(it.content).components,
                            lastChapter = it.lastChapter,
                            nextChapter = it.nextChapter
                        )
                    }
                }
        }
}