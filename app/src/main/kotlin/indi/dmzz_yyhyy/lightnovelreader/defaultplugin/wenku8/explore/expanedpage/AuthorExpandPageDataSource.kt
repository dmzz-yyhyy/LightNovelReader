package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.expanedpage

import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter
import io.nightfish.lightnovelreader.api.web.search.SearchProvider

internal class AuthorExpandPageDataSource(
    private val author: String,
    private val searchProvider: SearchProvider
) : ExploreExpandedPageDataSource {
    override val title = "作者：$author"
    override val filters: List<Filter<*>> = emptyList()

    // Wenku8's search flow already fetches every result page.
    override fun loadMore() = Unit

    override fun getResultFlow() = searchProvider.search(
        searchProvider.searchTypes.first { it.type == "author" },
        author
    )
}
