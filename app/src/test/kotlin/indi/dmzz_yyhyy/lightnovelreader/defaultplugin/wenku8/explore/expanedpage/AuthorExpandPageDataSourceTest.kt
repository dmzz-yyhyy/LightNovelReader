package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.expanedpage

import io.nightfish.lightnovelreader.api.util.local
import io.nightfish.lightnovelreader.api.web.search.SearchProvider
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import io.nightfish.lightnovelreader.api.web.search.SearchType
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthorExpandPageDataSourceTest {
    @Test
    fun authorSearchPreservesResultsAndCanBeRefreshed() = runBlocking {
        val requests = mutableListOf<Pair<String, String>>()
        val results = listOf(SearchResult.SingleBook("1"), SearchResult.MultipleBook("2"), SearchResult.End())
        val provider = object : SearchProvider {
            override val searchTypes = listOf(
                SearchType("articlename", "Title".local(), "".local()),
                SearchType("author", "Author".local(), "".local())
            )

            override fun search(searchType: SearchType, keyword: String) =
                flowOf(*results.toTypedArray()).also { requests.add(searchType.type to keyword) }
        }
        val page = AuthorExpandPageDataSource("川原 砾", provider)
        assertEquals("作者：川原 砾", page.title)
        assertEquals(results, page.getResultFlow().toList())
        page.loadMore()
        assertEquals(1, requests.size)
        assertEquals(results, page.getResultFlow().toList())
        assertEquals(List(2) { "author" to "川原 砾" }, requests)
    }
}
