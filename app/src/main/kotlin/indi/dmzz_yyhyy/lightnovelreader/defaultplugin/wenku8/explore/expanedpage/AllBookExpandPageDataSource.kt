package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.expanedpage

import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.autoReconnectionGetWithWenku8Cookie
import indi.dmzz_yyhyy.lightnovelreader.utils.network.selectFirstXpath
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter
import io.nightfish.lightnovelreader.api.web.explore.filter.LocalFilter
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.seconds

class HomeBookExpandPageDataSource(
    private val baseUrl: String = "${Wenku8Api.host}/modules/article/articlelist.php",
    private val extendedParameters: String = "",
    private val contentSelector: String = "#content > table.grid > tbody > tr > td > div",
    override val title: String,
    filtersBuilder: HomeBookExpandPageDataSource.() -> List<Filter<*>>,
): ExploreExpandedPageDataSource {
    override val filters = filtersBuilder(this)
    private var maxPage = 1
    private var targetPage = 1
    private var currentPage = 1
    var arg = ""

    override fun getResultFlow(): Flow<SearchResult>  = flow {
        maxPage = 1
        targetPage = 1
        currentPage = 1
        val localFilter: List<LocalFilter> = filters.mapNotNull { it as? LocalFilter }
        while(targetPage <= maxPage) {
            if (targetPage < currentPage) {
                delay(1)
                continue
            }
            val soup = autoReconnectionGetWithWenku8Cookie("${baseUrl}?page=$currentPage$arg$extendedParameters")
            if (soup == null) {
                emit(SearchResult.Error("Failed to request the web page"))
                return@flow
            }
            val menu = soup.selectFirstXpath("//*[@id=\"content\"]/div[1]/div[4]/div/span[1]/fieldset/div/a")
            if (menu != null && menu.text().contains("小说目录")) {
                val id = menu.attr("href").split("/").getOrNull(3)
                if (id == null) {
                    emit(SearchResult.Error("Failed to parse single book id"))
                    return@flow
                }
                emit(SearchResult.SingleBook(id))
                return@flow
            }
            if (maxPage == 1) {
                val page = soup.selectFirstXpath("//*[@id=\"pagelink\"]/em")?.text()?.split("/")?.getOrNull(1)?.toIntOrNull()
                if (page == null) {
                    emit(SearchResult.Error("Failed to request the web page"))
                    return@flow
                }
                maxPage = page
            }

            val books = Wenku8Api.getBookInformationListFromBookCards(soup.select(contentSelector))
            for (information in books) {
                if (localFilter.all { it.filter(information) }) {
                    emit(SearchResult.MultipleBook(information))
                }
            }
            currentPage++
            delay(1.seconds)
        }
        emit(SearchResult.End())
    }

    override fun loadMore() {
        targetPage = maxPage.coerceAtMost(targetPage + 1)
    }
}