package indi.dmzz_yyhyy.lightnovelreader.data.explore

import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import io.nightfish.lightnovelreader.api.web.search.SearchType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreRepository @Inject constructor(
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
) {
    val searchTypes get() = webBookDataSourceProvider.value.searchProvider.searchTypes
    val authorSearchType: SearchType?
        get() {
            if (!webBookDataSourceProvider.isWebDataSourceFounded()) return null
            val provider = webBookDataSourceProvider.value.searchProvider
            val declared = provider.authorSearchType ?: return null
            return provider.searchTypes.firstOrNull { it.type == declared.type }
        }
    val explorePageProvider get() = webBookDataSourceProvider.value.explorePageProvider

    fun search(searchType: SearchType, keyword: String): Flow<SearchResult> {
        val provider = webBookDataSourceProvider.value.searchProvider
        val isAuthorSearch = searchType.type == authorSearchType?.type
        return provider.search(searchType, keyword).transform { result ->
            if (isAuthorSearch && result is SearchResult.SingleBook) {
                // Author searches always show a list, even when only one work matches.
                emit(SearchResult.MultipleBook(result.bookId))
                emit(SearchResult.End())
            } else {
                emit(result)
            }
        }
    }

    fun getSuggestions(history: List<String>, keyword: String): List<String> = webBookDataSourceProvider.value.searchProvider.getSearchSuggestions(history, keyword)
}