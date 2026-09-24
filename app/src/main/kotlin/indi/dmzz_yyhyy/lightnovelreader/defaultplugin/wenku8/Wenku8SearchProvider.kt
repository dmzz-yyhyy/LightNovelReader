package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8

import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.book.BookRequestDispatcher
import io.nightfish.lightnovelreader.api.util.local
import io.nightfish.lightnovelreader.api.web.search.AbstractSearchProvider
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import io.nightfish.lightnovelreader.api.web.search.SearchType
import kotlinx.coroutines.flow.Flow

class Wenku8SearchProvider(
    val dispatcher: BookRequestDispatcher
): AbstractSearchProvider() {
    override fun search(
        searchType: SearchType,
        keyword: String
    ): Flow<SearchResult> {
        return dispatcher.search(searchType.type, keyword)
    }

    init {
        registerSearchType("articlename", "按书名搜索".local(), "请输入书本名称".local())
        registerSearchType("author", "按作者名搜索".local(), "请输入作者名称".local())
    }
}