package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import android.util.Log
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onOk
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.content.ContentComponentRepository
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.ReadingStatsUpdate
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.ChapterContentUiState
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.UserReadingData
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import io.nightfish.lightnovelreader.api.userdata.update
import io.nightfish.lightnovelreader.api.web.WebDataSourcePriority
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val bookRepository: BookRepository,
    userDataRepository: UserDataRepository,
    val contentComponentRepository: ContentComponentRepository
) : ViewModel() {
    val settingState = SettingState(userDataRepository, viewModelScope)
    private val _uiState = MutableReaderScreenUiState()
    val uiState: ReaderScreenUiState = _uiState
    private val readingBookListUserData =
        userDataRepository.userData(UserDataPath.ReadingBooks)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var volumeCollectJob: Job? = null
    private var currentChapterContentJob: Job? = null
    private var prevChapterContentJob: Job? = null
    private var nextChapterContentJob: Job? = null
    private var prevRequestId: Identifier.Chapter? = null
    private var nextRequestId: Identifier.Chapter? = null
    private var entryProgress: Float? = null
    private var reportedProgressChapter: Identifier.Chapter? = null
    private var lastQueuedProgress: ReadingPosition? = null
    private val readingDataMutex = Mutex()
    private val progressUpdates = Channel<ReadingPosition>(Channel.UNLIMITED)

    init {
        coroutineScope.launch {
            for (position in progressUpdates) {
                try {
                    saveReadingProgress(position)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Log.e("ReaderViewModel", "Failed to save reading progress for ${position.chapterId}", error)
                }
            }
        }
    }

    override fun onCleared() {
        flushReadingProgress()
        progressUpdates.close()
    }

    private suspend fun addToReadingBook(bookId: Identifier.Book) {
        readingBookListUserData.update {
            val newList = it.toMutableList()
            if (it.contains(bookId))
                newList.remove(bookId)
            newList.add(bookId)
            return@update newList
        }
    }

    fun changeBookId(bookId: Identifier.Book) {
        if (uiState.bookId == bookId) return
        flushReadingProgress()
        currentChapterContentJob?.cancel()
        cancelAdjacentRequests()
        reportedProgressChapter = null
        Snapshot.withMutableSnapshot {
            _uiState.bookId = bookId
            _uiState.chapterId = null
            _uiState.bookVolumes = null
            _uiState.userReadingData = null
            _uiState.readingProgress = null
            _uiState.currentChapterContent = null
            _uiState.preChapterContent = null
            _uiState.nextChapterContent = null
        }
        viewModelScope.launch(Dispatchers.IO) {
            addToReadingBook(bookId)
            statsRepository.updateReadingStatistics(
                ReadingStatsUpdate(bookId = bookId, readEventDelta = 1)
            )
        }
        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) { bookRepository.getUserReadingData(bookId) }
            if (uiState.bookId != bookId) return@launch
            Snapshot.withMutableSnapshot {
                _uiState.userReadingData = data
                _uiState.readingProgress = entryProgress
                    ?: data.savedProgress(uiState.chapterId)
            }
        }
        volumeCollectJob?.cancel()
        volumeCollectJob = viewModelScope.launch {
            bookRepository.getBookVolumesFlow(bookId)
                .flowOn(Dispatchers.IO)
                .collect {
                    _uiState.bookVolumes = it
                    uiState.currentChapterContent?.get()?.let(::updateAdjacentChapters)
                    flushReadingProgress()
                }
        }
    }

    private fun Flow<Result<ChapterContent, WebRequestError>>.mapToContentUiState() =
        this.map { result ->
            result.map { chapter ->
                ChapterContentUiState(
                    id = chapter.id,
                    title = chapter.title,
                    content = contentComponentRepository
                        .getContentDataListFromJson(chapter.content),
                    prevChapter = chapter.prevChapter,
                    nextChapter = chapter.nextChapter
                )
            }
        }.catch { error ->
            Log.e("ReaderViewModel", "Failed to prepare chapter content", error)
            emit(Err(WebRequestError("Chapter content failed", error.message.orEmpty(), error)))
        }

    private fun UserReadingData.savedProgress(chapterId: Identifier.Chapter?): Float =
        currentChapterReadingProgressMap[chapterId]?.takeIf(Float::isFinite)?.coerceIn(0f, 1f) ?: 0f

    fun changeChapter(chapterId: Identifier.Chapter) = changeChapter(chapterId, null)

    private fun changeChapter(chapterId: Identifier.Chapter, progress: Float?) {
        val bookId = uiState.bookId ?: return
        if (chapterId == uiState.chapterId) return
        flushReadingProgress()
        reportedProgressChapter = null
        entryProgress = progress
        val loaded = listOfNotNull(
            uiState.preChapterContent,
            uiState.currentChapterContent,
            uiState.nextChapterContent,
        ).associateBy { it.get()?.id }
        val promoted = loaded[chapterId]
        currentChapterContentJob?.cancel()
        cancelAdjacentRequests()
        Snapshot.withMutableSnapshot {
            _uiState.chapterId = chapterId
            _uiState.readingProgress = progress
                ?: uiState.userReadingData?.savedProgress(chapterId) ?: 0f
            _uiState.currentChapterContent = promoted
            _uiState.preChapterContent = null
            _uiState.nextChapterContent = null
            promoted?.get()?.let { updateAdjacentChapters(it, loaded) }
        }
        if (promoted != null) return
        currentChapterContentJob = viewModelScope.launch {
            bookRepository.getChapterContentFlow(chapterId, bookId, WebDataSourcePriority.High)
                .mapToContentUiState()
                .flowOn(Dispatchers.IO)
                .collect { result ->
                    Snapshot.withMutableSnapshot {
                        _uiState.currentChapterContent = result
                        result.onOk { updateAdjacentChapters(it) }
                    }
                }
        }
    }

    private fun cancelAdjacentRequests() {
        prevChapterContentJob?.cancel()
        nextChapterContentJob?.cancel()
        prevRequestId = null
        nextRequestId = null
    }

    private fun updateAdjacentChapters(
        chapter: ChapterContentUiState,
        loaded: Map<Identifier.Chapter?, Result<ChapterContentUiState, WebRequestError>> = emptyMap(),
    ) {
        val bookId = uiState.bookId ?: return
        val volumes = uiState.bookVolumes?.get()
        val previous = chapter.previousId(volumes)
        val next = chapter.nextId(volumes)
        if (previous != prevRequestId) {
            prevChapterContentJob?.cancel()
            prevRequestId = previous
            _uiState.preChapterContent = loaded[previous]?.takeIf { previous != null }
            prevChapterContentJob = if (_uiState.preChapterContent == null && previous != null) {
                loadAdjacent(bookId, previous) { _uiState.preChapterContent = it }
            } else null
        }
        if (next != nextRequestId) {
            nextChapterContentJob?.cancel()
            nextRequestId = next
            _uiState.nextChapterContent = loaded[next]?.takeIf { next != null }
            nextChapterContentJob = if (_uiState.nextChapterContent == null && next != null) {
                loadAdjacent(bookId, next) { _uiState.nextChapterContent = it }
            } else null
        }
    }

    private fun loadAdjacent(
        bookId: Identifier.Book,
        chapterId: Identifier.Chapter,
        onLoaded: (Result<ChapterContentUiState, WebRequestError>) -> Unit,
    ) = viewModelScope.launch {
        bookRepository.getChapterContentFlow(chapterId, bookId, WebDataSourcePriority.Low)
            .mapToContentUiState()
            .flowOn(Dispatchers.IO)
            .collect(onLoaded)
    }

    fun updateReadingProgress(chapterId: Identifier.Chapter, progress: Float, settled: Boolean) {
        if (chapterId != uiState.chapterId || !progress.isFinite()) return
        reportedProgressChapter = chapterId
        _uiState.readingProgress = progress.coerceIn(0f, 1f)
        if (settled) flushReadingProgress()
    }

    fun flushReadingProgress() {
        val bookId = uiState.bookId ?: return
        val chapter = uiState.currentChapterContent?.get() ?: return
        val progress = uiState.readingProgress ?: return
        if (reportedProgressChapter != chapter.id) return
        val totalChapters = uiState.bookVolumes?.get()?.allChapters?.size ?: 0
        val position = ReadingPosition(bookId, chapter.id, chapter.title, progress, totalChapters)
        if (position == lastQueuedProgress) return
        lastQueuedProgress = position
        _uiState.userReadingData = uiState.userReadingData
            ?.copyWithUpdatedChapterReadingProgress(chapter.id, progress)
        progressUpdates.trySend(position)
    }

    private suspend fun saveReadingProgress(position: ReadingPosition) {
        val total = position.totalChapters
        val currentTime = LocalDateTime.now()
        var finished = false
        readingDataMutex.withLock {
            bookRepository.updateUserReadingData(position.bookId) { data ->
                val updated = data.copyWithUpdatedChapterReadingProgress(position.chapterId, position.progress)
                val progress = if (total > 0) {
                    (updated.maxChapterReadingProgressMap.values.sum() / total).coerceIn(0f, 1f)
                } else data.readingProgress
                finished = data.readingProgress < 1f && progress >= 1f
                updated.copy(
                    lastReadTime = currentTime,
                    lastReadChapterId = position.chapterId,
                    lastReadChapterTitle = position.title,
                    readingProgress = progress,
                )
            }
        }
        if (finished) statsRepository.markBookFinished(position.bookId)
    }

    private data class ReadingPosition(
        val bookId: Identifier.Book,
        val chapterId: Identifier.Chapter,
        val title: String,
        val progress: Float,
        val totalChapters: Int,
    )

    fun updateTotalReadingTime(bookId: Identifier.Book, totalReadingTime: Int) {
        coroutineScope.launch {
            readingDataMutex.withLock {
                bookRepository.updateUserReadingData(bookId) {
                    it.copy(
                        lastReadTime = LocalDateTime.now(),
                        totalReadTime = it.totalReadTime + totalReadingTime
                    )
                }
            }
        }
    }

    fun accumulateReadingTime(bookId: Identifier.Book, seconds: Int) {
        coroutineScope.launch {
            statsRepository.accumulateBookReadTime(bookId, seconds)
        }
    }

    fun prevChapter() {
        uiState.currentChapterContent?.get()?.previousId(uiState.bookVolumes?.get())
            ?.let { changeChapter(it, 1f) }
    }

    fun nextChapter() {
        uiState.currentChapterContent?.get()?.nextId(uiState.bookVolumes?.get())
            ?.let { changeChapter(it, 0f) }
    }
}
