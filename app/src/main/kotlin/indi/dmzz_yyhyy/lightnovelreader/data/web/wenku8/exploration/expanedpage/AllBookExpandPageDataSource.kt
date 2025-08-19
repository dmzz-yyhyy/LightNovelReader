package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.expanedpage

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationExpandedPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.Filter
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.LocalFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.Wenku8Api.getBookInformationListFromBookCards
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.Wenku8Api.host
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.wenku8Cookie
import indi.dmzz_yyhyy.lightnovelreader.utils.autoReconnectionGet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jsoup.Jsoup

class HomeBookExpandPageDataSource(
    private val baseUrl: String = "$host/modules/article/articlelist.php",
    private val extendedParameters: String = "",
    private val title: String,
    filtersBuilder: HomeBookExpandPageDataSource.() -> List<Filter>,
    private val contentSelector: String = "#content > table.grid > tbody > tr > td > div"
): ExplorationExpandedPageDataSource {
    private val result = MutableStateFlow(listOf<BookInformation>())
    private val filter = filtersBuilder(this)
    private var cache = emptyList<BookInformation>()
    private var pageIndex = 1
    private var hasMore = true
    var arg = ""

    override fun getTitle(): String = title

    override fun getFilters(): List<Filter> = filter

    override fun getResultFlow(): Flow<List<BookInformation>> = result

    override fun refresh() {
        pageIndex = 1
        hasMore = true
        cache = emptyList()
        result.update { emptyList() }
    }

    override suspend fun loadMore() {
        result.update { bookInformationList ->
            if (cache.isEmpty()) bookInformationList + getBooks(pageIndex)
            else bookInformationList + cache
        }
        if (hasMore) cache = getBooks(pageIndex)
    }

    override fun hasMore(): Boolean = hasMore

    private suspend fun getBooks(
        pageIndex: Int,
        min: Int = 10
    ): List<BookInformation> =
        Jsoup
            .connect("${baseUrl}?page=$pageIndex$arg$extendedParameters")
            .wenku8Cookie()
            .autoReconnectionGet()
            ?.let { document ->
                document.selectFirst("#pagelink > a.last")?.text()?.toInt()?.let {
                    if (it == pageIndex) hasMore = false
                }
                return@let document
            }
            ?.select(contentSelector)
            ?.let {
                this.pageIndex++
                getBookInformationListFromBookCards(it)
            }
            ?.filter { bookInformation ->
                getFilters()
                    .filter { it is LocalFilter }
                    .map { it as LocalFilter }
                    .all { it.filter(bookInformation) }
            }
            ?.let {
                if (it.size <= min && hasMore) it + getBooks(this.pageIndex, min - it.size) else it
            } ?: emptyList()
}