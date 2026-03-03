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

object Wenku8AllExploreTapPage: ExploreTapPageDataSource {
    override val title = "全部"

    override fun getRowsFlow(): Flow<List<ExploreBooksRow>> = flow {
        val rows = mutableListOf<ExploreBooksRow>()
        rows.add(getAllBookBooksRow().copy(expandable = true, expandedPageDataSourceId = "allBook"))
        emit(rows)
        rows.add(getTopListBookBooksRow("热门轻小说", "allvisit"))
        emit(rows)
        rows.add(getTopListBookBooksRow("动画化作品", "anime"))
        emit(rows)
        rows.add(getTopListBookBooksRow("今日更新", "lastupdate"))
        emit(rows)
        rows.add(getTopListBookBooksRow("新书一览", "postdate"))
        emit(rows)
        rows.add(getCompletedBooksRow().copy(expandable = true, expandedPageDataSourceId = "allCompletedBook"))
        emit(rows)
    }

    private suspend fun getCompletedBooksRow(): ExploreBooksRow {
        val soup = autoReconnectionGetWithWenku8Cookie("${host}/modules/article/articlelist.php?fullflag=1")
        return getBooksRow(soup, "完结全本").copy(
            expandable = true,
            expandedPageDataSourceId = "allBook"
        )
    }

    private suspend fun getTopListBookBooksRow(title: String, sort: String): ExploreBooksRow {
        val soup = autoReconnectionGetWithWenku8Cookie("${host}/modules/article/toplist.php?sort=$sort")
        return getBooksRow(soup, title).copy(
            expandable = true,
            expandedPageDataSourceId = "${sort}Book"
        )
    }

    private suspend fun getAllBookBooksRow(): ExploreBooksRow {
        val soup = autoReconnectionGetWithWenku8Cookie("${host}/modules/article/articlelist.php")
        return getBooksRow(soup, "轻小说列表")
    }

    private fun getBooksRow(soup: Document?, title: String): ExploreBooksRow {
        val idlList = soup?.select("#content > table.grid > tbody > tr > td > div > div:nth-child(1) > a")
            ?.slice(0..5)
            ?.map { it.attr("href").replace("/book/", "").replace(".htm", "") }
        val titleList = soup?.select("#content > table.grid > tbody > tr > td > div > div:nth-child(2) > b > a")
            ?.slice(0..5)
            ?.map { it.text().split("(").getOrNull(0) ?: "" } ?: emptyList()
        val authorList = soup?.select("#content > table.grid > tbody > tr > td > div > div:nth-child(2) > p:nth-child(2)")
            ?.slice(0..5)
            ?.map { it.text().split("/").getOrNull(0)?.split(":")?.get(1) ?: ""} ?: emptyList()
        val coverUrlList = soup?.select("#content > table.grid > tbody > tr > td > div > div:nth-child(1) > a > img")
            ?.slice(0..5)
            ?.map { it.attr("src") } ?: emptyList()
        return ExploreBooksRow(
            title = title,
            bookList = idlList?.indices?.map {
                ExploreDisplayBook(
                    id = idlList[it],
                    title = titleList[it],
                    author = authorList[it],
                    coverUri = coverUrlList[it].toUri(),
                )
            } ?: emptyList(),
            expandable = false
        )
    }
}