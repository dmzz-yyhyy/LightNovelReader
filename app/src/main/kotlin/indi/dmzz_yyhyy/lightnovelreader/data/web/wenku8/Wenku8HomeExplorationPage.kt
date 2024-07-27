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

object Wenku8HomeExplorationPage: ExplorationPageDataSource {
    override fun getExplorationPage(): Flow<ExplorationPage>  {
        val explorationPage: MutableStateFlow<ExplorationPage> = MutableStateFlow(ExplorationPage.empty())
        CoroutineScope(Dispatchers.Default).launch {
            val soup = Jsoup
                .connect("https://www.wenku8.cc/")
                .wenku8Cookie()
                .get()
            explorationPage.update{ ExplorationPage(
                "首页",
                (0..2).map {
                    getBooksRow(it, soup)
                })
            }
        }
        return explorationPage
    }

    private fun getBooksRow(index: Int, soup: Document): ExplorationBooksRow {
        val title = soup.selectFirst("#centers > div:nth-child(${index+2}) > div.blocktitle")?.text()
            ?.split("(")?.getOrNull(0) ?: ""
        val idlList = soup.select("#centers > div:nth-child(${index+2}) > div.blockcontent > div > div > a:nth-child(1)")
            .map { it.attr("href").replace("/book/", "").replace(".htm", "").toInt() }
        val titleList = soup.select("#centers > div:nth-child(${index+2}) > div.blockcontent > div > div > a:nth-child(3)")
            .map { it.text().split("(").getOrNull(0) ?: "" }
        val coverUrlList = soup.select("#centers > div:nth-child(${index+2}) > div.blockcontent > div > div > a:nth-child(1) > img")
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