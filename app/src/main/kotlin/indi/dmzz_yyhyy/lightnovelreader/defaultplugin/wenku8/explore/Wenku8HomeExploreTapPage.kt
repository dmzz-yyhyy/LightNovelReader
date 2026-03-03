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

object Wenku8HomeExploreTapPage: ExploreTapPageDataSource {
    override val title = "首页"

    override fun getRowsFlow(): Flow<List<ExploreBooksRow>> = flow {
        val rows = mutableListOf<ExploreBooksRow>()
        val soup = autoReconnectionGetWithWenku8Cookie(host)
        (0..2).map { index->
            rows.add(getBooksRow(index, soup))
            emit(rows)
        }
    }
    private fun getBooksRow(index: Int, soup: Document?): ExploreBooksRow {
        val title = soup?.selectFirst("#centers > div:nth-child(${index+2}) > div.blocktitle")?.text()
            ?.split("(")?.getOrNull(0) ?: ""
        val idlList = soup?.select("#centers > div:nth-child(${index+2}) > div.blockcontent > div > div > a:nth-child(1)")
            ?.map { it.attr("href").replace("/book/", "").replace(".htm", "") }
        val titleList = soup?.select("#centers > div:nth-child(${index+2}) > div.blockcontent > div > div > a:nth-child(3)")
            ?.map { it.text().split("(").getOrNull(0) ?: "" } ?: emptyList()
        val coverUrlList = soup?.select("#centers > div:nth-child(${index+2}) > div.blockcontent > div > div > a:nth-child(1) > img")
            ?.map { it.attr("src") } ?: emptyList()
        return ExploreBooksRow(
            title = title,
            bookList = idlList?.indices?.map {
                ExploreDisplayBook(
                    id = idlList[it],
                    title = titleList[it],
                    author = "",
                    coverUri = coverUrlList[it].toUri(),
                )
            } ?: emptyList(),
            expandable = false
        )
    }
}