package indi.dmzz_yyhyy.lightnovelreader.data

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationExpandedPageDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Singleton
class ExplorationRepository @Inject constructor(
    private val webBookDataSource: WebBookDataSource
) {
    private val searchResultCacheMap = mutableMapOf<String, List<BookInformation>>()
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    suspend fun getExplorationPageMap() = webBookDataSource.getExplorationPageMap()
    suspend fun getExplorationPageTitleList() = webBookDataSource.getExplorationPageTitleList()
    fun getExplorationExpandedPageDataSource(expandedPageDataSourceId: String): ExplorationExpandedPageDataSource? =
        webBookDataSource.getExplorationExpandedPageDataSourceMap()[expandedPageDataSourceId]

    fun search(searchType: String, keyword: String): Flow<List<BookInformation>> {
        searchResultCacheMap[searchType + keyword]?.let { searchResult ->
            val flow = MutableStateFlow(emptyList<BookInformation>())
            flow.update { searchResult }
            return flow
        }
        val flow = webBookDataSource.search(searchType, keyword)
        coroutineScope.launch {
            flow.collect {
                if (it.isNotEmpty() && it.last().isEmpty()) {
                    searchResultCacheMap[searchType + keyword] = it
                }
            }
        }

        return flow
    }
    fun stopAllSearch() = webBookDataSource.stopAllSearch()
    fun getSearchTypeNameList(): List<String> = webBookDataSource.getSearchTypeNameList()
    fun getSearchTypeMap(): Map<String, String> = webBookDataSource.getSearchTypeMap()
    fun getSearchTipMap(): Map<String, String> = webBookDataSource.getSearchTipMap()
}