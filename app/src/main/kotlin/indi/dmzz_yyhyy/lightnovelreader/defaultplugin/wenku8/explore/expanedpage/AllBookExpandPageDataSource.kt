package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.expanedpage

import com.github.michaelbull.result.Result
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.utils.network.selectFirstXpath
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.nodes.Document
import kotlin.time.Duration.Companion.seconds

class HomeBookExpandPageDataSource(
    val host: String,
    val wenku8Api: Wenku8Api,
    private val baseUrl: String = "${host}/modules/article/articlelist.php",
    private val extendedParameters: String = "",
    private val contentSelector: String = "#content > table.grid > tbody > tr > td > div",
    override val title: String,
    filtersBuilder: HomeBookExpandPageDataSource.() -> List<Filter<*>>,
    internal val requestPage: suspend (String) -> Result<Document, Throwable> = wenku8Api::getWithWenku8Cookie,
) : ExploreExpandedPageDataSource {
    override val filters = filtersBuilder(this)
    @Volatile
    private var loadRequests: Channel<Unit>? = null
    var arg = ""

    override fun getResultFlow(): Flow<SearchResult> = flow {
        val requests = Channel<Unit>(Channel.CONFLATED)
        loadRequests = requests
        var maxPage = 1
        var currentPage = 1
        try {
            while (currentPage <= maxPage) {
                if (currentPage > 1) {
                    requests.receive()
                    delay(1.seconds)
                }
                val soup = requestPage("${baseUrl}?page=$currentPage$arg$extendedParameters").component1()
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
                if (currentPage == 1) {
                    val page = soup.selectFirstXpath("//*[@id=\"pagelink\"]/em")?.text()?.split("/")?.getOrNull(1)?.toIntOrNull()
                    if (page == null) {
                        emit(SearchResult.Error("Failed to request the web page"))
                        return@flow
                    }
                    maxPage = page
                }

                val books = wenku8Api.getBookInformationListFromBookCards(soup.select(contentSelector))
                for (pairs in books) {
                    emit(SearchResult.MultipleBook(pairs.first))
                }
                currentPage++
            }
            emit(SearchResult.End())
        } finally {
            requests.close()
            loadRequests = null
        }
    }

    override fun loadMore() {
        loadRequests?.trySend(Unit)
    }
}
