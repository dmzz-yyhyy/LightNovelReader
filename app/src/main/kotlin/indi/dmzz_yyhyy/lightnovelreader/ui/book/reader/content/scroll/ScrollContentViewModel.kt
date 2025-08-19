package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.scroll

import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.IntSize
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ContentViewModel
import indi.dmzz_yyhyy.lightnovelreader.utils.throttleLatest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ScrollContentViewModel(
    val bookRepository: BookRepository,
    val coroutineScope: CoroutineScope,
    val settingState: SettingState,
    val updateReadingProgress: (Float) -> Unit
) : ContentViewModel {
    private var progressScrollLoadJob: Job? = null
    private val loadChapterJobs: MutableList<Job> = mutableListOf()
    private var lazyColumnSize = IntSize(0, 0)
    private var lastWriteReadingProgress = 0L

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
        coroutineScope.launch(Dispatchers.IO) {
            settingState.isUsingContinuousScrollingUserData.getFlowWithDefault(false).collect {
                if (it) {
                    progressScrollLoad()
                    if (uiState.contentList.size == 1)
                        coroutineScope.launch(Dispatchers.Main) {
                            changeChapter(uiState.readingContentId)
                        }
                } else {
                    progressScrollLoadJob?.cancel()
                    if (uiState.contentList.size > 1)
                        coroutineScope.launch(Dispatchers.Main) {
                            changeChapter(uiState.readingContentId)
                        }
                }
            }
        }
        coroutineScope.launch(Dispatchers.Main) {
            snapshotFlow { uiState.lazyListState.firstVisibleItemScrollOffset }
                .throttleLatest(120L)
                .collect {
                    val layoutInfo = uiState.lazyListState.layoutInfo
                    val item = layoutInfo.visibleItemsInfo.firstOrNull {
                        it.key == uiState.readingChapterContent.id
                    } ?: return@collect

                    val newProgress = 1f.coerceAtMost((-item.offset + lazyColumnSize.height).toFloat() / item.size)
                    if (newProgress == uiState.readingProgress) return@collect

                    uiState.readingProgress = newProgress

                    val now = System.currentTimeMillis()
                    if (now - lastWriteReadingProgress < 2500 && newProgress < 1f) return@collect

                    lastWriteReadingProgress = now
                    coroutineScope.launch(Dispatchers.IO) {
                        updateReadingProgress(newProgress)
                    }
                }
        }

    }

    private fun writeProgressRightNow() {
        updateReadingProgress(uiState.readingProgress)
    }

    private fun progressScrollLoad() {
        progressScrollLoadJob?.cancel()
        progressScrollLoadJob = coroutineScope.launch(Dispatchers.IO) {
            snapshotFlow { uiState.lazyListState.layoutInfo.visibleItemsInfo.getOrNull(0) }.collect { itemInfo ->
                if (uiState.lazyListState.layoutInfo.visibleItemsInfo.size == 1 &&
                    itemInfo?.key?.equals(uiState.readingChapterContent.lastChapter) == true &&
                    lazyColumnSize.height != 0 &&
                    itemInfo.offset <= -lazyColumnSize.height &&
                    !uiState.readingChapterContent.isEmpty() &&
                    uiState.readingChapterContent.hasLastChapter()
                ) {
                    if (uiState.contentList.getOrNull(uiState.contentList.size - 1)?.id == uiState.readingChapterContent.nextChapter)
                        uiState.contentList.removeAt(uiState.contentList.size - 1)
                    uiState.readingContentId = uiState.readingChapterContent.lastChapter
                    if (uiState.readingChapterContent.hasLastChapter())
                        uiState.contentList.add(
                            0,
                            bookRepository.getStateChapterContent(
                                uiState.readingChapterContent.lastChapter,
                                uiState.bookId,
                                coroutineScope
                            )
                        )
                    bookRepository.updateUserReadingData(uiState.bookId) {
                        it.apply {
                            lastReadChapterProgress =
                                if (it.lastReadChapterId == uiState.readingChapterContent.id) it.lastReadChapterProgress else 0f
                            lastReadTime = LocalDateTime.now()
                            lastReadChapterId = uiState.readingChapterContent.id
                            lastReadChapterTitle = uiState.readingChapterContent.title
                        }
                    }
                    return@collect
                }
                if (
                    itemInfo?.key?.equals(uiState.readingChapterContent.nextChapter) == true &&
                    !uiState.readingChapterContent.isEmpty() &&
                    uiState.readingChapterContent.hasNextChapter()
                ) {
                    if (uiState.contentList.getOrNull(0)?.id == uiState.readingChapterContent.lastChapter)
                        uiState.contentList.removeAt(0)
                    uiState.readingContentId = uiState.readingChapterContent.nextChapter
                    if (uiState.readingChapterContent.hasLastChapter())
                        uiState.contentList.add(
                            bookRepository.getStateChapterContent(
                                uiState.readingChapterContent.nextChapter,
                                uiState.bookId,
                                coroutineScope
                            )
                        )
                    bookRepository.updateUserReadingData(uiState.bookId) {
                        it.apply {
                            lastReadChapterProgress =
                                if (it.lastReadChapterId == uiState.readingChapterContent.id) it.lastReadChapterProgress else 0f
                            lastReadTime = LocalDateTime.now()
                            lastReadChapterId = uiState.readingChapterContent.id
                            lastReadChapterTitle = uiState.readingChapterContent.title
                        }
                    }
                    return@collect
                }
            }
        }
    }

    override fun changeBookId(id: Int) {
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
        if (!uiState.readingChapterContent.hasLastChapter()) return
        coroutineScope.launch {
            changeChapter(
                id = uiState.readingChapterContent.lastChapter
            )
        }
    }

    override fun changeChapter(id: Int) {
        loadChapterJobs.forEach(Job::cancel)
        uiState.contentList.clear()
        uiState.readingContentId = id
        uiState.readingProgress = 0f
        coroutineScope.launch(Dispatchers.IO) {
            val chapterContent = bookRepository.getChapterContent(id, uiState.bookId)
            bookRepository.updateUserReadingData(uiState.bookId) {
                it.apply {
                    lastReadChapterProgress =
                        if (it.lastReadChapterId == id) it.lastReadChapterProgress else 0f
                    lastReadTime = LocalDateTime.now()
                    lastReadChapterId = id
                    lastReadChapterTitle = chapterContent.title
                }
            }
        }.let(loadChapterJobs::add)
        coroutineScope.launch(Dispatchers.IO) {
            val isUsingContinuousScrollingUserData =
                settingState.isUsingContinuousScrollingUserData.getOrDefault(false)
            bookRepository.getChapterContentFlow(id, uiState.bookId, coroutineScope).collect { chapterContent ->
                if (chapterContent.isEmpty()) return@collect
                if (chapterContent.id != uiState.readingContentId) return@collect
                if (
                    chapterContent.content == uiState.readingChapterContent.content &&
                    chapterContent.title == uiState.readingChapterContent.title &&
                    chapterContent.lastChapter == uiState.readingChapterContent.lastChapter &&
                    chapterContent.nextChapter == uiState.readingChapterContent.nextChapter
                ) return@collect
                uiState.contentList.clear()
                if (chapterContent.hasLastChapter() && isUsingContinuousScrollingUserData) {
                    uiState.contentList.add(
                        bookRepository.getStateChapterContent(
                            chapterContent.lastChapter,
                            uiState.bookId,
                            coroutineScope
                        )
                    )
                }
                uiState.contentList.add(chapterContent)
                if (chapterContent.hasNextChapter() && isUsingContinuousScrollingUserData) {
                    uiState.contentList.add(
                        bookRepository.getStateChapterContent(
                            chapterContent.nextChapter,
                            uiState.bookId,
                            coroutineScope
                        )
                    )
                } else if (chapterContent.hasNextChapter()) {
                    bookRepository.getChapterContent(chapterContent.nextChapter, uiState.bookId)
                }
                val userReadingData = bookRepository.getUserReadingData(uiState.bookId)
                if (userReadingData.lastReadChapterId == id)
                    uiState.readingProgress = userReadingData.lastReadChapterProgress
                coroutineScope.launch {
                    val itemIndex = uiState.contentList.indexOfFirst { it.id == id }
                    if (itemIndex >= 0) {
                        uiState.lazyListState.scrollToItem(itemIndex)
                        uiState.lazyListState.scrollToItem(
                            itemIndex,
                            uiState.lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == id }
                                ?.let {
                                    if (userReadingData.lastReadChapterId == uiState.readingChapterContent.id)
                                        ((it.size * userReadingData.lastReadChapterProgress).toInt() - lazyColumnSize.height).coerceAtLeast(
                                            0
                                        )
                                    else
                                        0
                                } ?: 0
                        )
                    }
                }.let(loadChapterJobs::add)
            }
        }.let(loadChapterJobs::add)
    }
}