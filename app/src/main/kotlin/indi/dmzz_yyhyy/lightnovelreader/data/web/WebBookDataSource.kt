package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent

interface WebBookDataSource {
    suspend fun getBookInformation(id: Int): BookInformation?
    suspend fun getBookVolumes(id: Int): BookVolumes?
    suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent?
}