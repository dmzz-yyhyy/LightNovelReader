package indi.dmzz_yyhyy.lightnovelreader.ui.reader

data class ReaderUiState (
    val isLoading: Boolean = true,
    val isBottomBarOpen: Boolean = false,
    val chapterId: Int = 0,
    val title: String = "",
    val content: String = "",
)