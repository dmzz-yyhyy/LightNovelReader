package indi.dmzz_yyhyy.lightnovelreader.ui.book.content

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData

@Stable
interface ContentScreenUiState {
    val isLoading: Boolean
    val chapterContent: ChapterContent
    val userReadingData: UserReadingData
    val bookVolumes: BookVolumes
    val fontSize: Float
    val fontLineHeight: Float
    val keepScreenOn: Boolean
}

class MutableContentScreenUiState: ContentScreenUiState {
    override var isLoading by mutableStateOf(true)
    override var chapterContent by mutableStateOf(ChapterContent.empty())
    override var userReadingData by mutableStateOf(UserReadingData.empty())
    override var bookVolumes by mutableStateOf(BookVolumes.empty())
    override var fontSize by mutableStateOf(14f)
    override var fontLineHeight by mutableStateOf(0f)
    override var keepScreenOn by mutableStateOf(false)
}