package indi.dmzz_yyhyy.lightnovelreader.ui.book.content

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent

@Stable
interface ContentScreenUiState {
    val chapterContent: ChapterContent
    val isLoading: Boolean
}

class MutableContentScreenUiState: ContentScreenUiState {
    override var chapterContent by mutableStateOf(ChapterContent.empty())
    override var isLoading by mutableStateOf(true)
}