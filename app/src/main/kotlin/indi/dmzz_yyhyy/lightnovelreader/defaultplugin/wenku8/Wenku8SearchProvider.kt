package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.book.BookRequestDispatcher
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.wenku8LocalFilters
import io.nightfish.lightnovelreader.api.web.OptionValues
import io.nightfish.lightnovelreader.api.web.search.AbstractSearchProvider
import io.nightfish.lightnovelreader.api.web.search.SearchOption
import io.nightfish.lightnovelreader.api.web.search.SearchSetting

class Wenku8SearchProvider(
    private val dispatcher: BookRequestDispatcher
) : AbstractSearchProvider() {
    private val scope = SearchSetting.SingleChoice(
        id = "scope",
        title = { stringResource(R.string.explore_search_scope) },
        options = listOf(
            SearchOption("articlename") { stringResource(R.string.explore_search_by_title) },
            SearchOption("author") { stringResource(R.string.explore_search_by_author) }
        ),
        defaultId = "articlename"
    )
    private val hints = mapOf(
        "articlename" to R.string.explore_search_title_hint,
        "author" to R.string.explore_search_author_hint
    )

    init {
        registerSearchSetting(scope)
        wenku8LocalFilters().forEach(::registerSearchFilter)
    }

    @Composable
    override fun getSearchTip(settings: OptionValues) = stringResource(hints.getValue(settings[scope.key]))

    override suspend fun loadPage(
        keyword: String,
        settings: OptionValues,
        filters: OptionValues,
        cursor: String?
    ) =
        dispatcher.search(settings[scope.key], keyword, cursor)
}
