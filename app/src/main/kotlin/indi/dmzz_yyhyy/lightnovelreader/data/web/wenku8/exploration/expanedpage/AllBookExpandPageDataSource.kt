package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.expanedpage

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationExpandedPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.Filter
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.IsCompletedSwitchFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.LocalFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.SingleChoiceFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.Wenku8Api.getBookInformationListFromBookCards
import indi.dmzz_yyhyy.lightnovelreader.utils.wenku8.wenku8Cookie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.jsoup.Jsoup

object AllBookExpandPageDataSource: ExplorationExpandedPageDataSource {
    private val choicesMap = mapOf(
        Pair("任意", ""),
        Pair("0~9", "1")
    )
    private val result = MutableStateFlow(listOf<BookInformation>())
    private val filters =
        listOf(
            IsCompletedSwitchFilter { this.refresh() },
            SingleChoiceFilter(
                title = "首字母",
                dialogTitle = "首字母筛选",
                description = "根据小说标题的拼音首字母筛选。",
                choices = listOf("任意", "0~9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"),
                defaultChoice = "任意"
            ) {
                arg = choicesMap[it] ?: it
                this.refresh()
            },
            SingleChoiceFilter(
                title = "文库",
                dialogTitle = "首字母筛选",
                description = "根据小说标题的拼音首字母筛选。",
                choices = listOf("任意", "0~9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"),
                defaultChoice = "任意"
            ) {
                arg = choicesMap[it] ?: it
                this.refresh()
            }
        )
    private var arg = ""
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
            .connect("https://www.wenku8.cc/modules/article/articlelist.php?page=$pageIndex&initial=$arg")
            .wenku8Cookie()
            .get()
            .let { document ->
                println("https://www.wenku8.cc/modules/article/articlelist.php?page=$pageIndex&initial=$arg")
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
                if (it.size <= min && hasMore) it + getBooks(AllBookExpandPageDataSource.pageIndex, min - it.size) else it
            }
}