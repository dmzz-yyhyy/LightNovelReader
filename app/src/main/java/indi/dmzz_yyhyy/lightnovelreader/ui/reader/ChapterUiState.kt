package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import indi.dmzz_yyhyy.lightnovelreader.data.`object`.Volume

data class ChapterUiState(
    val isLoading: Boolean = true,
    val isChapterReversed: Boolean = false,
    val isVolumeReversed: Boolean = false,
    val bookName: String = "",
    val bookCoverUrl: String = "",
    val bookIntroduction: String = "",
    val volumeList: List<Volume> = listOf()
)
