package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration

import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationBooksRow
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationDisplayBook
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationPage
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.Wenku8Api.host
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.wenku8Cookie
import indi.dmzz_yyhyy.lightnovelreader.utils.autoReconnectionGet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object Wenku8AllExplorationPage: ExplorationPageDataSource {
    private var lock = false
    private val explorationBooksRows: MutableStateFlow<List<ExplorationBooksRow>> = MutableStateFlow(emptyList())

    override val title = "全部"

    override fun getExplorationPage(): ExplorationPage  {
        if (!lock) {
            lock = true
            CoroutineScope(Dispatchers.IO).launch {
                explorationBooksRows.update {
                    it + getAllBookBooksRow().copy(expandable = true, expandedPageDataSourceId = "allBook")
                }
                explorationBooksRows.update {
                    it + getTopListBookBooksRow("热门轻小说", "allvisit")
                }
                explorationBooksRows.update {
                    it + getTopListBookBooksRow("动画化作品", "anime")
                }
                explorationBooksRows.update {
                    it + getTopListBookBooksRow("今日更新", "lastupdate")
                }
                explorationBooksRows.update {
                    it + getTopListBookBooksRow("新书一览", "postdate")
                }
                explorationBooksRows.update {
                    it + getCompletedBooksRow().copy(expandable = true, expandedPageDataSourceId = "allCompletedBook")
                }
            }
        }
        return ExplorationPage("首页", explorationBooksRows)
    }

    private suspend fun getCompletedBooksRow(): ExplorationBooksRow {
        val soup = Jsoup
            .connect("$host/modules/article/articlelist.php?fullflag=1")
            .wenku8Cookie()
            .autoReconnectionGet()
        return getBooksRow(soup, "完结全本").copy(
            expandable = true,
            expandedPageDataSourceId = "allBook"
        )
    }

    private suspend fun getTopListBookBooksRow(title: String, sort: String): ExplorationBooksRow {
        val soup = Jsoup
            .connect("$host/modules/article/toplist.php?sort=$sort")
            .wenku8Cookie()
            .autoReconnectionGet()
        return getBooksRow(soup, title).copy(
            expandable = true,
            expandedPageDataSourceId = "${sort}Book"
        )
    }

    private suspend fun getAllBookBooksRow(): ExplorationBooksRow {
        val soup = Jsoup
            .connect("$host/modules/article/articlelist.php")
            .wenku8Cookie()
            .autoReconnectionGet()
        return getBooksRow(soup, "轻小说列表")
    }

    private fun getBooksRow(soup: Document?, title: String): ExplorationBooksRow {
        val idlList = soup?.select("#content > table.grid > tbody > tr > td > div > div:nth-child(1) > a")
            ?.slice(0..5)
            ?.map { it.attr("href").replace("/book/", "").replace(".htm", "").toInt() }
        val titleList = soup?.select("#content > table.grid > tbody > tr > td > div > div:nth-child(2) > b > a")
            ?.slice(0..5)
            ?.map { it.text().split("(").getOrNull(0) ?: "" } ?: emptyList()
        val authorList = soup?.select("#content > table.grid > tbody > tr > td > div > div:nth-child(2) > p:nth-child(2)")
            ?.slice(0..5)
            ?.map { it.text().split("/").getOrNull(0)?.split(":")?.get(1) ?: ""} ?: emptyList()
        val coverUrlList = soup?.select("#content > table.grid > tbody > tr > td > div > div:nth-child(1) > a > img")
            ?.slice(0..5)
            ?.map { it.attr("src") } ?: emptyList()
        return ExplorationBooksRow(
            title = title,
            bookList = idlList?.indices?.map {
                ExplorationDisplayBook(
                    id = idlList[it],
                    title = titleList[it],
                    author = authorList[it],
                    coverUrl = coverUrlList[it],
                )
            } ?: emptyList(),
            expandable = false
        )
    }
}