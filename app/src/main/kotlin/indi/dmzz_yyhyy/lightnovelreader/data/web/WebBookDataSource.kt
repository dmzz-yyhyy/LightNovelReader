package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationExpandedPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import kotlinx.coroutines.flow.Flow

interface WebBookDataSource {
    suspend fun getIsOffLineFlow(): Flow<Boolean>
    suspend fun getBookInformation(id: Int): BookInformation?
    suspend fun getBookVolumes(id: Int): BookVolumes?
    suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent?
    suspend fun getExplorationPageMap(): Map<String, ExplorationPageDataSource>
    suspend fun getExplorationPageTitleList(): List<String>
    fun getExplorationExpandedPageDataSourceMap(): Map<String, ExplorationExpandedPageDataSource>
    fun search(searchType: String, keyword: String): Flow<List<BookInformation>>
    fun getSearchTypeMap(): Map<String, String>
    fun getSearchTipMap(): Map<String, String>
    fun getSearchTypeNameList(): List<String>
    fun stopAllSearch()
}