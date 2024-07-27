package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8

import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationBooksRow
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationDisplayBook
import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationPage
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.utils.wenku8.wenku8Cookie
import java.net.URLEncoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object Wenku8TagsExplorationPage: ExplorationPageDataSource {
    override fun getExplorationPage(): Flow<ExplorationPage>  {
        val explorationPage: MutableStateFlow<ExplorationPage> = MutableStateFlow(ExplorationPage.empty())
        CoroutineScope(Dispatchers.Default).launch {
            explorationPage.update{ ExplorationPage(
                "首页",
                Jsoup
                    .connect("https://www.wenku8.cc/modules/article/tags.php")
                    .wenku8Cookie()
                    .get()
                    .select("a[href~=tags\\.php\\?t=.*]")
                    .slice(0..48)
                    .map { "https://www.wenku8.cc/modules/article/" + it.attr("href") }
                    .map {
                        getExplorationBookRow(
                            soup = Jsoup
                                .connect(it.split("=")[0] + "=" + URLEncoder.encode(it.split("=")[1], "gb2312"))
                                .wenku8Cookie()
                                .get(),
                            title = it.split("=")[1]
                        )
                    }
                )
            }
        }
        return explorationPage
    }

    private fun getExplorationBookRow(title: String, soup: Document): ExplorationBooksRow {
        val idlList = soup.select("#content > table > tbody > tr:nth-child(2) > td > div > div:nth-child(1) > a")
            .map { it.attr("href").replace("/book/", "").replace(".htm", "").toInt() }
        val titleList = soup.select("#content > table > tbody > tr:nth-child(2) > td > div > div:nth-child(2) > b > a")
            .map { it.text().split("(").getOrNull(0) ?: "" }
        val coverUrlList = soup.select("#content > table > tbody > tr:nth-child(2) > td > div > div:nth-child(1) > a > img")
            .map { it.attr("src") }
        return ExplorationBooksRow(
            title = title,
            bookList = (0..5).map {
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