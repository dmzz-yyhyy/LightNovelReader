package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.Filter
import kotlinx.coroutines.flow.Flow

interface ExplorationExpandedPageDataSource {
    fun getTitle(): String
    fun getFilters(): List<Filter>
    fun getResultFlow(): Flow<List<BookInformation>>
    fun refresh()
    fun loadMore()
    fun hasMore(): Boolean
}