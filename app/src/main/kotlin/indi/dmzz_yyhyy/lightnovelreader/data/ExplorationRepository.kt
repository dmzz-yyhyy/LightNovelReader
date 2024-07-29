package indi.dmzz_yyhyy.lightnovelreader.data

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ExplorationRepository @Inject constructor(
    private val webBookDataSource: WebBookDataSource
) {
    suspend fun getExplorationPageMap() = webBookDataSource.getExplorationPageMap()
    suspend fun getExplorationPageTitleList() = webBookDataSource.getExplorationPageTitleList()

    fun search(searchType: String, keyword: String): Flow<List<BookInformation>> = webBookDataSource.search(searchType, keyword)
    fun stopAllSearch() = webBookDataSource.stopAllSearch()
    fun getSearchTypeNameList(): List<String> = webBookDataSource.getSearchTypeNameList()
    fun getSearchTypeMap(): Map<String, String> = webBookDataSource.getSearchTypeMap()
    fun getSearchTipMap(): Map<String, String> = webBookDataSource.getSearchTipMap()
}