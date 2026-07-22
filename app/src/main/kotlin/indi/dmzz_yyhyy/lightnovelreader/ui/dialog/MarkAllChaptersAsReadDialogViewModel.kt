package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MarkAllChaptersAsReadDialogViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val statsRepository: StatsRepository
) : ViewModel() {
    var bookVolumeResult: Result<BookVolumes, WebRequestError>? by mutableStateOf(null)
        private set

    var bookId by mutableStateOf("")
        private set

    private var volumesJob: Job? = null

    fun load(bookId: String) {
        if (bookId == this.bookId) return
        this.bookId = bookId
        volumesJob?.cancel()
        volumesJob = viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getBookVolumesFlow(bookId).collect { result ->
                bookVolumeResult = result
            }
        }
    }

    fun markAllChaptersAsRead() = binding {
        if (bookId.isBlank()) return@binding Unit

        val allChapterIds = bookVolumeResult
            ?.bind()
            ?.volumes
            ?.flatMap { it.chapters }
            ?.map { it.id } ?: return@binding Unit

        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateUserReadingData(bookId) { userReadingData ->
                val newProgress = userReadingData.maxChapterReadingProgressMap.toMutableMap()
                for (id in allChapterIds) {
                    newProgress[id] = 1f
                }
                userReadingData.copy(
                    lastReadTime = LocalDateTime.now(),
                    readingProgress = if (allChapterIds.isEmpty()) 0f else 1f,
                    maxChapterReadingProgressMap = newProgress
                )
            }
            statsRepository.markBookFinished(bookId)
        }
    }

    fun markChaptersAsRead(chapterIds: List<String>) = binding {
        if (bookId.isBlank()) return@binding Unit

        val allChapterIds = bookVolumeResult
            ?.bind()
            ?.volumes
            ?.flatMap { it.chapters }
            ?.map { it.id } ?: return@binding Unit

        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateUserReadingData(bookId) { userReadingData ->
                val newProgress = userReadingData.maxChapterReadingProgressMap.toMutableMap()
                for (id in chapterIds) {
                    newProgress[id] = 1f
                }
                userReadingData.copy(
                    lastReadTime = LocalDateTime.now(),
                    readingProgress =
                        if (allChapterIds.isEmpty())
                            0f
                        else
                            userReadingData.currentChapterReadingProgressMap.values.sum() / allChapterIds.size,
                    maxChapterReadingProgressMap = newProgress
                )
            }
            val readingData = bookRepository.getUserReadingData(bookId)
            if (readingData.readingProgress >= 1f) {
                statsRepository.markBookFinished(bookId)
            }
        }
    }
}