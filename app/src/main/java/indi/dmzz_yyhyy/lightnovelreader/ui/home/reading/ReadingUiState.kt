package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import java.time.LocalDateTime

data class ReadingBook(
    private val bookInformation: BookInformation,
    private val userReadingData: UserReadingData
) {
    val id: Int get() = bookInformation.id
    val title: String get() = bookInformation.title
    val coverUrl: String get() = bookInformation.coverUrl
    val author: String get() = bookInformation.author
    val description: String get() = bookInformation.description
    val publishingHouse : String get() = bookInformation.publishingHouse
    val lastReadTime: LocalDateTime get() = userReadingData.lastReadTime
    val totalReadTime: Int get() = userReadingData.totalReadTime
    val readingProgress: Double get() = userReadingData.readingProgress
    val lastReadChapterId: Int get() = userReadingData.lastReadChapterId
    val lastReadChapterTitle: String get() = userReadingData.lastReadChapterTitle
}

@Stable
interface ReadingUiState {
    val recentReadingBooks: List<ReadingBook>
    val isLoading: Boolean
}

class MutableReadingUiState: ReadingUiState {
    override var recentReadingBooks: MutableList<ReadingBook> by mutableStateOf(mutableListOf())
    override var isLoading: Boolean by mutableStateOf(true)
}