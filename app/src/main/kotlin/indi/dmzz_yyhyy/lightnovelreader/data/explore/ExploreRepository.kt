package indi.dmzz_yyhyy.lightnovelreader.data.explore

import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import io.nightfish.lightnovelreader.api.web.search.SearchType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreRepository @Inject constructor(
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
) {
    val searchTypes get() = webBookDataSourceProvider.value.searchProvider.searchTypes
    val explorePageProvider get() = webBookDataSourceProvider.value.explorePageProvider

    fun search(searchType: SearchType, keyword: String): Flow<SearchResult> =
        webBookDataSourceProvider.value.searchProvider.search(searchType, keyword)

    fun getSuggestions(history: List<String>, keyword: String): List<String> = webBookDataSourceProvider.value.searchProvider.getSearchSuggestions(history, keyword)
}