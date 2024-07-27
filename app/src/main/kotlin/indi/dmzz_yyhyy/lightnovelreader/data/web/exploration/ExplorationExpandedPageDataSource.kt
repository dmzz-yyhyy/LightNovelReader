package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.Filter

interface ExplorationExpandedPageDataSource {
    fun getFilters(): List<Filter>
    fun getResult(): List<BookInformation>
}