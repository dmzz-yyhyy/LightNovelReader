package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import indi.dmzz_yyhyy.lightnovelreader.data.`object`.Volume

data class ReaderUiState(
    val isLoading: Boolean = true,
    val isBottomBarOpen: Boolean = false,
    val isSideSheetsOpen: Boolean = false,
    val chapterId: Int = 0,
    val title: String = "",
    val content: String = "",
    val volumeList: List<Volume> = listOf(),
)