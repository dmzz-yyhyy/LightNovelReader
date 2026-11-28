package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.expanedpage

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.parseBookPage
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.redirectedBookId
import io.nightfish.lightnovelreader.api.error.WebRequestError
import io.nightfish.lightnovelreader.api.error.mapAsWebRequestError
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.BookPageResult
import io.nightfish.lightnovelreader.api.web.OptionValues
import io.nightfish.lightnovelreader.api.web.PageBook
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter

class HomeBookExpandPageDataSource(
    host: String,
    private val wenku8Api: Wenku8Api,
    override val title: String,
    override val filters: List<Filter<*>>,
    private val baseUrl: String = "$host/modules/article/articlelist.php",
    private val extendedParameters: String = "",
    private val contentSelector: String = "#content > table.grid > tbody > tr > td > div",
    private val queryOptions: (OptionValues) -> String = { "" }
) : ExploreExpandedPageDataSource {
    override suspend fun loadPage(
        cursor: String?,
        filters: OptionValues
    ): Result<BookPageResult.Page, WebRequestError> =
        coroutineBinding {
            val page = cursor?.toInt() ?: 1
            val url = "$baseUrl?page=$page${queryOptions(filters)}$extendedParameters"
            val document = wenku8Api.getWithWenku8Cookie(url)
                .mapAsWebRequestError("探索页加载失败", "无法请求探索页书目")
                .bind()
            val directBook = document.redirectedBookId()
            if (directBook != null) {
                val id = wenku8Api.id.child(Identifier::Book, directBook)
                BookPageResult.Page(listOf(PageBook(id, wenku8Api.getBookInformation(id))), null)
            } else {
                wenku8Api.parseBookPage(document, contentSelector, page)
            }
        }
}
