package indi.dmzz_yyhyy.lightnovelreader.data.explore

import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyWebBookDataSource
import io.nightfish.lightnovelreader.api.util.local
import io.nightfish.lightnovelreader.api.web.search.SearchProvider
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import io.nightfish.lightnovelreader.api.web.search.SearchType
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy

class AuthorSearchTest {
    @Test
    fun authorCapabilityIsOptInAndSearchKeepsSingleResultsInTheList() = runBlocking {
        val titleType = SearchType("title", "Title".local(), "".local())
        // Capability is explicit: neither this identifier nor its label is interpreted by the host.
        val authorType = SearchType("writer-id", "Writer".local(), "".local())
        val requests = mutableListOf<Pair<String, String>>()
        val single = SearchResult.SingleBook("42")
        val legacyProvider = object : SearchProvider {
            override val searchTypes = listOf(titleType, authorType)
            override fun search(searchType: SearchType, keyword: String) = flowOf(single)
        }
        assertTrue(SearchProvider::class.java.getMethod("getAuthorSearchType").isDefault)
        assertNull(legacyProvider.authorSearchType)
        assertNull(repository(legacyProvider).authorSearchType)
        assertNull(repository(legacyProvider, available = false).authorSearchType)

        var declaredType: SearchType? = authorType
        var results: List<SearchResult> = listOf(single)
        val provider = object : SearchProvider {
            override val searchTypes = listOf(titleType, authorType)
            override val authorSearchType get() = declaredType
            override fun search(searchType: SearchType, keyword: String) =
                flowOf(*results.toTypedArray()).also { requests.add(searchType.type to keyword) }
        }
        val repository = repository(provider)
        assertEquals(authorType, repository.authorSearchType)
        val authorResults = repository.search(authorType, "川原 砾").toList()
        assertEquals("42", (authorResults.first() as SearchResult.MultipleBook).bookId)
        assertTrue(authorResults.last() is SearchResult.End)
        assertEquals(listOf("writer-id" to "川原 砾"), requests)
        assertSame(single, repository.search(titleType, "Book").toList().single())

        results = listOf(SearchResult.MultipleBook("1"), SearchResult.MultipleBook("2"), SearchResult.End())
        assertEquals(results, repository.search(authorType, "川原 砾").toList())
        results = listOf(SearchResult.Empty(), SearchResult.End())
        assertEquals(results, repository.search(authorType, "Nobody").toList())
        results = listOf(SearchResult.Error("offline"))
        assertEquals(results, repository.search(authorType, "Writer").toList())

        declaredType = SearchType("unregistered", "Writer".local(), "".local())
        assertNull(repository.authorSearchType)
    }

    private fun repository(provider: SearchProvider, available: Boolean = true): ExploreRepository {
        val source = Proxy.newProxyInstance(
            ProxyWebBookDataSource::class.java.classLoader,
            arrayOf(ProxyWebBookDataSource::class.java)
        ) { _, method, _ ->
            check(method.name == "getSearchProvider")
            provider
        } as ProxyWebBookDataSource
        return ExploreRepository(object : WebBookDataSourceProvider {
            override fun isWebDataSourceFounded() = available
            override val value get() = source.also { check(available) }
        })
    }
}
