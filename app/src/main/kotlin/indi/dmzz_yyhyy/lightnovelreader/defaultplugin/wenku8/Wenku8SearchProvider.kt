package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8

import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.book.BookRequestDispatcher
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.wenku8LocalFilters
import io.nightfish.lightnovelreader.api.util.local
import io.nightfish.lightnovelreader.api.web.OptionValues
import io.nightfish.lightnovelreader.api.web.search.AbstractSearchProvider
import io.nightfish.lightnovelreader.api.web.search.SearchOption
import io.nightfish.lightnovelreader.api.web.search.SearchSetting

class Wenku8SearchProvider(
    private val dispatcher: BookRequestDispatcher
) : AbstractSearchProvider() {
    private val scope = SearchSetting.SingleChoice(
        id = "scope",
        title = "搜索范围".local(),
        options = listOf(
            SearchOption("articlename", "按书名搜索".local()),
            SearchOption("author", "按作者名搜索".local())
        ),
        defaultId = "articlename"
    )
    private val hints = mapOf(
        "articlename" to "请输入书本名称".local(),
        "author" to "请输入作者名称".local()
    )

    init {
        registerSearchSetting(scope)
        wenku8LocalFilters().forEach(::registerSearchFilter)
    }

    override fun getSearchTip(settings: OptionValues) = hints.getValue(settings[scope.key])

    override suspend fun loadPage(keyword: String, settings: OptionValues, filters: OptionValues, cursor: String?) =
        dispatcher.search(settings[scope.key], keyword, cursor)
}
