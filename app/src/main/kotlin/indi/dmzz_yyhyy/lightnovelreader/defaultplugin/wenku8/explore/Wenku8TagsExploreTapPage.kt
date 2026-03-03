package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore

import androidx.core.net.toUri
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api.host
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.autoReconnectionGetWithWenku8Cookie
import io.nightfish.lightnovelreader.api.explore.ExploreBooksRow
import io.nightfish.lightnovelreader.api.explore.ExploreDisplayBook
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Document
import java.net.URLEncoder

object Wenku8TagsExploreTapPage: ExploreTapPageDataSource {
    override val title = "分类"


    override fun getRowsFlow(): Flow<List<ExploreBooksRow>> = flow {
        val rows = mutableListOf<ExploreBooksRow>()
        autoReconnectionGetWithWenku8Cookie("${host}/modules/article/tags.php")
            ?.select("a[href~=tags\\.php\\?t=.*]")
            ?.slice(0..48)
            ?.map { "${host}/modules/article/" + it.attr("href") }
            ?.forEach { url ->
                val soup = autoReconnectionGetWithWenku8Cookie(url.split("=")[0] + "=" +
                        URLEncoder.encode(url.split("=")[1], "gb2312"))
                rows.add(
                    getExploreBookRow(
                        soup = soup,
                        title = url.split("=")[1]
                    )
                )
                emit(rows)
            }
    }

    private fun getExploreBookRow(title: String, soup: Document?): ExploreBooksRow {
        soup ?: return ExploreBooksRow(
            "",
            emptyList(),
            false,
            ""
        )
        val idlList = soup.select("#content > table > tbody > tr:nth-child(2) > td > div > div:nth-child(1) > a")
            .map { it.attr("href").replace("/book/", "").replace(".htm", "") }
        val titleList = soup.select("#content > table > tbody > tr:nth-child(2) > td > div > div:nth-child(2) > b > a")
            .map { it.text().split("(").getOrNull(0) ?: "" }
        val authorList = soup.select("#content > table > tbody > tr:nth-child(2) > td > div > div:nth-child(2) > p:nth-child(2)")
            .slice(0..5)
            .map { it.text().split("/").getOrNull(0)?.split(":")?.get(1) ?: ""}
        val coverUrlList = soup.select("#content > table > tbody > tr:nth-child(2) > td > div > div:nth-child(1) > a > img")
            .map { it.attr("src") }
        return ExploreBooksRow(
            title = title,
            bookList = (0..5).map {
                ExploreDisplayBook(
                    id = idlList[it],
                    title = titleList[it],
                    author = authorList[it],
                    coverUri = coverUrlList[it].toUri(),
                )
            },
            expandable = true,
            expandedPageDataSourceId = title
        )
    }
}