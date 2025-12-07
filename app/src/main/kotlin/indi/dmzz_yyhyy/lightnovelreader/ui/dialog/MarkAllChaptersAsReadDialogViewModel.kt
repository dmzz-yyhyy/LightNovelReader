package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import io.nightfish.lightnovelreader.api.book.BookVolumes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MarkAllChaptersAsReadDialogViewModel @Inject constructor(
    private val bookRepository: BookRepository
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
            bookRepository.getBookVolumesFlow(bookId, viewModelScope).collect { volumes ->
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
                    lastReadChapterProgress = 1f
                    readCompletedChapterIds.clear()
                    readCompletedChapterIds.addAll(allChapterIds)
                    readingProgress = if (allChapterIds.isEmpty()) 0f else 1f
                }
            }
        }
    }

    fun markChaptersAsRead(chapterIds: List<String>) {
        if (bookId.isBlank() || chapterIds.isEmpty()) return

        val allChapterIds = bookVolumes.volumes.flatMap { it.chapters }.map { it.id }

        viewModelScope.launch(Dispatchers.IO) {
            bookRepository.updateUserReadingData(bookId) { userReadingData ->
                userReadingData.apply {
                    lastReadTime = LocalDateTime.now()
                    val set = readCompletedChapterIds.toMutableSet()
                    set.addAll(chapterIds)
                    readCompletedChapterIds.clear()
                    readCompletedChapterIds.addAll(set)
                    readingProgress = if (allChapterIds.isEmpty()) 0f
                    else set.size.toFloat() / allChapterIds.size
                }
            }
        }
    }
}