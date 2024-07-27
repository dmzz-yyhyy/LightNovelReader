package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource

interface WebBookDataSource {
    suspend fun getBookInformation(id: Int): BookInformation?
    suspend fun getBookVolumes(id: Int): BookVolumes?
    suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent?
    suspend fun getExplorationPageMap(): Map<String, ExplorationPageDataSource>
    suspend fun getExplorationPageTitleList(): List<String>
}