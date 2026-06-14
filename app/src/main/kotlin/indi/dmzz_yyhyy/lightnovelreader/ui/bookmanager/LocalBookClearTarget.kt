package indi.dmzz_yyhyy.lightnovelreader.ui.bookmanager

import androidx.annotation.StringRes
import indi.dmzz_yyhyy.lightnovelreader.R

enum class LocalBookClearTarget(
    @param:StringRes val label: Int
) {
    VolumeAndChapterIndex(R.string.local_book_clear_index),
    ChapterContent(R.string.local_book_clear_content),
    ReadingRecord(R.string.local_book_clear_reading)
}
