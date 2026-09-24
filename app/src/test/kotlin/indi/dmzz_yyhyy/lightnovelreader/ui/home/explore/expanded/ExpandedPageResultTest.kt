package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExpandedPageResultTest {
    @Test
    fun firstResultStopsLoadingWhileOnlyTerminalResultsStopPagination() = runBlocking {
        val page = MutableExpandedPageUiState()
        assertTrue(page.acceptResult(SearchResult.MultipleBook("1")) { emptyFlow() })
        assertFalse(page.isLoading)
        assertFalse(page.isComplete)
        page.acceptResult(SearchResult.MultipleBook("1")) { error("Duplicate fetched twice") }
        assertEquals(listOf("1"), page.bookList.map { it.first })
        assertFalse(page.acceptResult(SearchResult.Error("offline")) { emptyFlow() })
        assertEquals("offline", page.errorMessage)
        assertEquals(1, page.bookList.size)
        assertTrue(page.isComplete)

        for (terminal in listOf(SearchResult.Empty(), SearchResult.End())) {
            val empty = MutableExpandedPageUiState()
            assertFalse(empty.acceptResult(terminal) { emptyFlow() })
            assertFalse(empty.isLoading)
            assertTrue(empty.isComplete)
            assertTrue(empty.bookList.isEmpty())
            assertNull(empty.errorMessage)
        }

        val single = MutableExpandedPageUiState()
        flowOf(SearchResult.SingleBook("only"), SearchResult.MultipleBook("unexpected"))
            .takeWhile { single.acceptResult(it) { emptyFlow() } }.toList()
        assertEquals(listOf("only"), single.bookList.map { it.first })
        assertTrue(single.isComplete)
        assertFalse(single.isLoading)
    }
}
