package indi.dmzz_yyhyy.lightnovelreader.data.book

import indi.dmzz_yyhyy.lightnovelreader.data.web.EmptyWebDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyWebBookDataSource
import io.nightfish.lightnovelreader.api.book.RelatedBookKind
import io.nightfish.lightnovelreader.api.book.RelatedBooksRequest
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Proxy

class RelatedBooksSourceTest {
    @Test
    fun invalidSourceOrRequestNeverCreatesAPageAndValidCallsGetFreshPages() {
        val requests = mutableListOf<RelatedBooksRequest>()
        val source = Proxy.newProxyInstance(
            ProxyWebBookDataSource::class.java.classLoader,
            arrayOf(ProxyWebBookDataSource::class.java, WebBookDataSource::class.java),
        ) { proxy, method, args ->
            when (method.name) {
                "getOrigin" -> proxy
                "getId" -> Identifier("site", "one")
                "getSupportedRelatedBookKinds" -> setOf(RelatedBookKind.AUTHOR)
                "createRelatedBooksPage" -> {
                    requests += args!![0] as RelatedBooksRequest
                    object : ExploreExpandedPageDataSource {
                        override val title = "author"
                        override val filters = emptyList<io.nightfish.lightnovelreader.api.web.explore.filter.Filter<*>>()
                        override fun loadMore() = Unit
                        override fun getResultFlow() = emptyFlow<SearchResult>()
                    }
                }
                else -> error("Unexpected call: ${method.name}")
            }
        } as ProxyWebBookDataSource
        var available = true
        var empty = false
        val provider = object : WebBookDataSourceProvider {
            override fun isWebDataSourceFounded() = available
            override val value: ProxyWebBookDataSource
                get() = if (empty) Proxy.newProxyInstance(
                    ProxyWebBookDataSource::class.java.classLoader,
                    arrayOf(ProxyWebBookDataSource::class.java),
                ) { _, method, _ ->
                    check(method.name == "getOrigin")
                    EmptyWebDataSource
                } as ProxyWebBookDataSource else source
        }
        val request = RelatedBooksRequest("42", RelatedBookKind.AUTHOR, "川原 礫")
        assertThrows(IllegalStateException::class.java) { provider.createRelatedBooksPage("site:other", request) }
        assertThrows(IllegalArgumentException::class.java) { provider.createRelatedBooksPage("site:one", request.copy(kind = RelatedBookKind.TAG)) }
        assertThrows(IllegalArgumentException::class.java) { provider.createRelatedBooksPage("site:one", request.copy(value = "  ")) }
        available = false
        assertNull(provider.relatedBooksSource())
        assertThrows(IllegalStateException::class.java) { provider.createRelatedBooksPage("site:one", request) }
        available = true
        empty = true
        assertNull(provider.relatedBooksSource())
        assertThrows(IllegalStateException::class.java) { provider.createRelatedBooksPage("site:one", request) }
        empty = false
        assertTrue(requests.isEmpty())
        assertNotSame(provider.createRelatedBooksPage("site:one", request), provider.createRelatedBooksPage("site:one", request))
        assertEquals(listOf(request, request), requests)
    }
}
