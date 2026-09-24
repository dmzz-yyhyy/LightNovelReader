package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8

import com.github.michaelbull.result.Ok
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.expanedpage.HomeBookExpandPageDataSource
import io.nightfish.lightnovelreader.api.book.RelatedBookKind
import io.nightfish.lightnovelreader.api.book.RelatedBooksRequest
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.jsoup.Jsoup
import org.junit.Assert.*
import org.junit.Test

class RelatedBooksPageTest {
    @Test
    fun paginationEndsAtLastPageAndRestartDiscardsPendingLoadMore() = runBlocking {
        val api = Wenku8Api()
        try {
            val pages = mutableListOf<Int>()
            var lastPage = 1
            val source = HomeBookExpandPageDataSource(
                host = "https://example.test",
                wenku8Api = api,
                title = "tag",
                filtersBuilder = { emptyList() },
                requestPage = { url ->
                    val page = Regex("page=(\\d+)").find(url)!!.groupValues[1].toInt()
                    pages.add(page)
                    Ok(Jsoup.parse("<div id='pagelink'><em>$page/$lastPage</em></div>"))
                },
            )
            withTimeout(5_000) {
                assertTrue(source.getResultFlow().toList().single() is SearchResult.End)
                assertEquals(listOf(1), pages)

                pages.clear()
                lastPage = 2
                val results = mutableListOf<SearchResult>()
                val first = launch(start = CoroutineStart.UNDISPATCHED) {
                    source.getResultFlow().toList(results)
                }
                assertEquals(listOf(1), pages)
                assertTrue(first.isActive)
                source.loadMore()
                source.loadMore()
                first.join()
                assertEquals(listOf(1, 2), pages)
                assertTrue(results.single() is SearchResult.End)

                pages.clear()
                val cancelled = launch(start = CoroutineStart.UNDISPATCHED) {
                    source.getResultFlow().toList()
                }
                source.loadMore()
                cancelled.cancelAndJoin()
                source.loadMore()
                val restarted = launch(start = CoroutineStart.UNDISPATCHED) {
                    source.getResultFlow().toList()
                }
                assertNull(withTimeoutOrNull(1_100) { restarted.join() })
                assertEquals(listOf(1, 1), pages)
                restarted.cancelAndJoin()
            }
        } finally {
            api.ktorClient.close()
        }
    }

    @Test
    fun relatedFactoriesKeepRawTitleAndCreateIndependentPageState() {
        val api = Wenku8Api()
        try {
            assertEquals(setOf(RelatedBookKind.AUTHOR, RelatedBookKind.TAG), api.supportedRelatedBookKinds)
            for (kind in api.supportedRelatedBookKinds) {
                val request = RelatedBooksRequest("42", kind, "原始值")
                val first = api.createRelatedBooksPage(request)
                val second = api.createRelatedBooksPage(request)
                assertEquals(request.value, first.title)
                assertNotSame(first, second)
                first.filters.zip(second.filters).forEach { (left, right) -> assertNotSame(left, right) }
            }
        } finally {
            api.ktorClient.close()
        }
    }
}
