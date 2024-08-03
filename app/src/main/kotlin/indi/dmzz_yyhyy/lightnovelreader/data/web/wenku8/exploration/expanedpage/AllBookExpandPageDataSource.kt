package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.expanedpage

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationExpandedPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.Filter
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.IsCompletedSwitchFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.LocalFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.Wenku8Api.getBookInformationListFromBookCards
import indi.dmzz_yyhyy.lightnovelreader.utils.wenku8.wenku8Cookie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jsoup.Jsoup

object AllBookExpandPageDataSource: ExplorationExpandedPageDataSource {
    private val result = MutableStateFlow(listOf<BookInformation>())
    private val filters =
        listOf(
            IsCompletedSwitchFilter { this.refresh() }
        )
    private var cache = emptyList<BookInformation>()
    private var pageIndex = 1
    private var hasMore = true

    override fun getTitle(): String = "轻小说列表"

    override fun getFilters(): List<Filter> = filters

    override fun getResultFlow(): Flow<List<BookInformation>> = result

    override fun refresh() {
        pageIndex = 1
        hasMore = true
        cache = emptyList()
        result.update { emptyList() }
    }

    override fun loadMore() {
        result.update { bookInformationList ->
            if (cache.isEmpty()) bookInformationList + getBooks(pageIndex)
            else bookInformationList + cache
        }
        if (hasMore) cache = getBooks(pageIndex)
    }

    override fun hasMore(): Boolean = hasMore

    private fun getBooks(
        pageIndex: Int,
        min: Int = 10
    ): List<BookInformation> =
        Jsoup
            .connect("https://www.wenku8.cc/modules/article/articlelist.php?page=$pageIndex")
            .wenku8Cookie()
            .get()
            .let { document ->
                document.selectFirst("#pagelink > a.last")?.text()?.toInt()?.let {
                    if (it == pageIndex) hasMore = false
                }
                return@let document
            }
            .select("#content > table.grid > tbody > tr > td > div")
            .let {
                AllBookExpandPageDataSource.pageIndex++
                getBookInformationListFromBookCards(it)
            }
            .filter { bookInformation ->
                getFilters()
                    .filter { it is LocalFilter }
                    .map { it as LocalFilter }
                    .all { it.filter(bookInformation) }
            }
            .let {
                if (it.size <= min) it + getBooks(AllBookExpandPageDataSource.pageIndex, min - it.size) else it
            }
}