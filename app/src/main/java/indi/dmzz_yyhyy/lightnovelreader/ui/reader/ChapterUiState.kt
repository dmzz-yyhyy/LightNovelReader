package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume

data class ChapterUiState(
    val isLoading: Boolean = true,
    val bookName: String = "",
    val bookCoverUrl: String = "",
    val bookIntroduction: String = "",
    val volumeList: List<Volume> = listOf()
)
