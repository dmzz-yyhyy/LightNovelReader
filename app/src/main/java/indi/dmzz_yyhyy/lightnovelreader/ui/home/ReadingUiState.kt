package indi.dmzz_yyhyy.lightnovelreader.ui.home

import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.ReadingBook


data class ReadingUiState(
    val readingBookDataList: List<ReadingBook> = listOf(),
)