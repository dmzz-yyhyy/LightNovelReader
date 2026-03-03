package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.IntSize
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.content.ContentComponentRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
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
                        coroutineScope.launch(Dispatchers.Main) { changeChapter(uiState.readingContentId) }
                    }
                } else {
                    progressScrollLoadJob?.cancel()
                    if (uiState.contentList.size > 1) {
                        coroutineScope.launch(Dispatchers.Main) { changeChapter(uiState.readingContentId) }
                    }
                }
            }
        }
        coroutineScope.launch(Dispatchers.Main) {
            snapshotFlow { uiState.lazyListState.firstVisibleItemScrollOffset }
                .throttleLatest(120L)
                .collect {
                    val layoutInfo = uiState.lazyListState.layoutInfo
                    val chapterId = uiState.readingChapterContent.id
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
                        val chapterId = uiState.readingChapterContent.id
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
        updateReadingProgress(uiState.readingChapterContent.id, uiState.readingProgress)
    }

    private fun progressScrollLoad() {
        progressScrollLoadJob?.cancel()
        progressScrollLoadJob = coroutineScope.launch {
            snapshotFlow { uiState.lazyListState.layoutInfo.visibleItemsInfo.getOrNull(0) }.collect { itemInfo ->
                if (itemInfo?.key == uiState.readingChapterContent.lastChapter &&
                    lazyColumnSize.height != 0 &&
                    itemInfo.offset <= -lazyColumnSize.height &&
                    !uiState.readingChapterContent.isEmpty() &&
                    uiState.readingChapterContent.hasPrevChapter()
                ) {
                    collectNextChapterJob?.cancel()
                    collectCurrentChapterJob?.cancel()
                    collectLastChapterJob?.cancel()
                    val chapter1 = uiState.contentList[1]
                    val chapter0 = uiState.contentList[0]
                    resetContentList()
                    uiState.contentList[2] = chapter1
                    uiState.contentList[1] = chapter0
                    collectNextChapterJob = collectChapter(2, uiState.readingContentId)
                    collectCurrentChapterJob = collectChapter(1, uiState.readingChapterContent.lastChapter)
                    uiState.readingContentId = uiState.readingChapterContent.lastChapter
                    bookRepository.updateUserReadingData(uiState.bookId) {
                        it.apply {
                            lastReadTime = LocalDateTime.now()
                            lastReadChapterId = uiState.readingChapterContent.id
                            lastReadChapterTitle = uiState.readingChapterContent.title
                        }
                    }
                    if (uiState.readingChapterContent.hasPrevChapter())
                        collectLastChapterJob = collectChapter(0, uiState.readingChapterContent.lastChapter)
                }
                if (
                    itemInfo?.key == uiState.readingChapterContent.nextChapter &&
                    !uiState.readingChapterContent.isEmpty() &&
                    uiState.readingChapterContent.hasNextChapter()
                ) {
                    collectNextChapterJob?.cancel()
                    collectCurrentChapterJob?.cancel()
                    collectLastChapterJob?.cancel()
                    val chapter1 = uiState.contentList[1]
                    val chapter2 = uiState.contentList[2]
                    resetContentList()
                    uiState.contentList[0] = chapter1
                    uiState.contentList[1] = chapter2
                    collectLastChapterJob = collectChapter(0, uiState.readingContentId)
                    collectCurrentChapterJob = collectChapter(1, uiState.readingChapterContent.nextChapter)
                    uiState.readingContentId = uiState.readingChapterContent.nextChapter
                    bookRepository.updateUserReadingData(uiState.bookId) {
                        it.apply {
                            lastReadTime = LocalDateTime.now()
                            lastReadChapterId = uiState.readingChapterContent.id
                            lastReadChapterTitle = uiState.readingChapterContent.title
                        }
                    }
                    if (uiState.readingChapterContent.hasNextChapter())
                        collectNextChapterJob = collectChapter(2, uiState.readingChapterContent.nextChapter)
                }
            }
        }
    }

    override fun changeBookId(id: String) {
        uiState.bookId = id
    }

    override fun loadNextChapter() {
        if (!uiState.readingChapterContent.hasNextChapter()) return
        coroutineScope.launch {
            changeChapter(
                id = uiState.readingChapterContent.nextChapter
            )
        }
    }

    override fun loadLastChapter() {
        if (!uiState.readingChapterContent.hasPrevChapter()) return
        coroutineScope.launch {
            changeChapter(
                id = uiState.readingChapterContent.lastChapter
            )
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
        uiState.readingContentId = id
        uiState.readingProgress = 0f
        uiState.lazyListState = LazyListState()
        coroutineScope.launch (Dispatchers.IO) {
            val isUsingContinuousScrolling = settingState.isUsingContinuousScrollingUserData.getOrDefault(true)
            if (isUsingContinuousScrolling) chapterChapterWithContinuousScrolling(id)
            else chapterChapterWithoutContinuousScrolling(id)
        }
    }

    private fun chapterChapterWithoutContinuousScrolling(id: String) {
        collectCurrentChapterJob?.cancel()
        collectCurrentChapterJob = coroutineScope.launch(Dispatchers.IO) {
            bookRepository.getChapterContentFlow(id, uiState.bookId).collect { content ->
                if (content.isEmpty()) return@collect
                uiState.contentList[1] = content
                uiState.contentComponentsMap[content.id] = contentComponentRepository.getContentDataFromJson(content.content).components
                bookRepository.updateUserReadingData(uiState.bookId) { userReadingData ->
                    uiState.readingProgress = userReadingData.currentChapterReadingProgressMap[id] ?: 0f
                    userReadingData.apply {
                        lastReadTime = LocalDateTime.now()
                        lastReadChapterId = id
                        lastReadChapterTitle = content.title
                    }
                }
                if (content.hasNextChapter()) {
                    bookRepository.getChapterContent(content.nextChapter, uiState.bookId)
                }
            }
        }
    }

    private fun chapterChapterWithContinuousScrolling(id: String) {
        collectCurrentChapterJob?.cancel()
        collectCurrentChapterJob = coroutineScope.launch(Dispatchers.IO) {
            bookRepository.getChapterContentFlow(id, uiState.bookId).collect { content ->
                if (content.isEmpty()) return@collect
                uiState.contentList[1] = content
                uiState.contentComponentsMap[content.id] = contentComponentRepository.getContentDataFromJson(content.content).components
                bookRepository.updateUserReadingData(uiState.bookId) { userReadingData ->
                    uiState.readingProgress = userReadingData.currentChapterReadingProgressMap[id] ?: 0f
                    userReadingData.apply {
                        lastReadTime = LocalDateTime.now()
                        lastReadChapterId = id
                        lastReadChapterTitle = content.title
                    }
                }
                if (content.hasPrevChapter()) {
                    collectLastChapterJob?.cancel()
                    collectLastChapterJob = collectChapter(0, content.lastChapter)
                }
                if (content.hasNextChapter()) {
                    collectNextChapterJob?.cancel()
                    collectNextChapterJob = collectChapter(2, content.nextChapter)
                }
            }
        }
    }

    private fun collectChapter(index: Int, chapterId: String) = coroutineScope.launch {
            bookRepository.getChapterContentFlow(chapterId, uiState.bookId)
                .collect { content ->
                    if (content.isEmpty()) return@collect
                    uiState.contentList[index] = content
                    uiState.contentComponentsMap[content.id] = contentComponentRepository.getContentDataFromJson(content.content).components
                }
        }
}