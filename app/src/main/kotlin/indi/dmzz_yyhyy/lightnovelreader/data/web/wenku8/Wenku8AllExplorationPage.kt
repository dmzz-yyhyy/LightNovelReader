package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8

import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationBooksRow
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationDisplayBook
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationPage
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.utils.wenku8.wenku8Cookie
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object Wenku8AllExplorationPage: ExplorationPageDataSource {
    override fun getExplorationPage(): Flow<ExplorationPage>  {
        val explorationPage: MutableStateFlow<ExplorationPage> = MutableStateFlow(ExplorationPage.empty())
        CoroutineScope(Dispatchers.Default).launch {
            explorationPage.update{ ExplorationPage(
                "首页",
                    listOf(
                        getAllBookBooksRow(),
                        getTopListBookBooksRow("热门轻小说", "allvisit"),
                        getTopListBookBooksRow("动画化作品", "anime"),
                        getTopListBookBooksRow("今日更新", "lastupdate"),
                        getTopListBookBooksRow("新书一览", "postdate"),
                        getCompletedBooksRow()
                    )
                )
            }
        }
        return explorationPage
    }

    private fun getCompletedBooksRow(): ExplorationBooksRow {
        val soup = Jsoup
            .connect("https://www.wenku8.cc/modules/article/articlelist.php?fullflag=1")
            .wenku8Cookie()
            .get()
        return getBooksRow(soup, "完结全本")
    }

    private fun getTopListBookBooksRow(title: String, sort: String): ExplorationBooksRow {
        val soup = Jsoup
            .connect("https://www.wenku8.cc/modules/article/toplist.php?sort=$sort")
            .wenku8Cookie()
            .get()
        return getBooksRow(soup, title)
    }

    private fun getAllBookBooksRow(): ExplorationBooksRow {
        val soup = Jsoup
            .connect("https://www.wenku8.cc/modules/article/articlelist.php")
            .wenku8Cookie()
            .get()
        return getBooksRow(soup, "轻小说列表")
    }

    private fun getBooksRow(soup: Document, title: String): ExplorationBooksRow {
        val idlList = soup.select("#content > table.grid > tbody > tr > td > div > div:nth-child(1) > a")
            .slice(0..5)
            .map { it.attr("href").replace("/book/", "").replace(".htm", "").toInt() }
        val titleList = soup.select("#content > table.grid > tbody > tr > td > div > div:nth-child(2) > b > a")
            .slice(0..5)
            .map { it.text().split("(").getOrNull(0) ?: "" }
        val coverUrlList = soup.select("#content > table.grid > tbody > tr > td > div > div:nth-child(1) > a > img")
            .slice(0..5)
            .map { it.attr("src") }
        return ExplorationBooksRow(
            title = title,
            bookList = idlList.indices.map {
                ExplorationDisplayBook(
                    id = idlList[it],
                    title = titleList[it],
                    coverUrl = coverUrlList[it],
                )
            },
            expandable = false
        )
    }
}