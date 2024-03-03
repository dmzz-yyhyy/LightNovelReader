package indi.dmzz_yyhyy.lightnovelreader.ui.home

import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.BookMetadata


data class ReadingUiState(
    val readingBookDataList: List<BookMetadata> = listOf(),
)