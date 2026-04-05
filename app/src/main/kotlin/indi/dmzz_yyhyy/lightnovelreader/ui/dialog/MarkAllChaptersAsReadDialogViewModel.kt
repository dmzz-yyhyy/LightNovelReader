package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.StatsRepository
import io.nightfish.lightnovelreader.api.book.BookVolumes
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

    var bookVolumes by mutableStateOf(BookVolumes.empty())
        private set

    var bookId by mutableStateOf("")
        private set

    private var volumesJob: Job? = null

    fun load(bookId: String) {
        if (bookId == this.bookId) return
        this.bookId = bookId

        volumesJob?.cancel()
        volumesJob = viewModelScope.launch(Dispatchers.IO) {
            bookRepository.getBookVolumesFlow(bookId).collect { volumes ->
                if (volumes.volumes.isEmpty()) return@collect
                bookVolumes = volumes
            }
        }
    }

    fun markAllChaptersAsRead() {
        if (bookId.isBlank()) return
        val allChapterIds = bookVolumes.volumes.flatMap { it.chapters }.map { it.id }

        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateUserReadingData(bookId) { userReadingData ->
                userReadingData.apply {
                    lastReadTime = LocalDateTime.now()
                    for (id in allChapterIds) {
                        updateChapterReadingProgress(id, 1f)
                    }
                    readingProgress = if (allChapterIds.isEmpty()) 0f else 1f
                }
            }
            statsRepository.markBookFinished(bookId)
        }
    }

    fun markChaptersAsRead(chapterIds: List<String>) {
        if (bookId.isBlank() || chapterIds.isEmpty()) return

        val allChapterIds = bookVolumes.volumes.flatMap { it.chapters }.map { it.id }

        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateUserReadingData(bookId) { userReadingData ->
                userReadingData.apply {
                    lastReadTime = LocalDateTime.now()
                    for (id in chapterIds) {
                        userReadingData.updateChapterReadingProgress(id, 1f)
                    }
                    readingProgress = if (allChapterIds.isEmpty()) 0f
                    else userReadingData.currentChapterReadingProgressMap.values.sum() / allChapterIds.size
                }
            }
            val readingData = bookRepository.getUserReadingData(bookId)
            if (readingData.readingProgress >= 1f) {
                statsRepository.markBookFinished(bookId)
            }
        }
    }
}