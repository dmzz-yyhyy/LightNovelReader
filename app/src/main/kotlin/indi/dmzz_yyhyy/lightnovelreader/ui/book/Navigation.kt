package indi.dmzz_yyhyy.lightnovelreader.ui.book

import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.bookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.bookReaderDestination

fun NavEntryScope.bookNavigation() {
    bookDetailDestination()
    bookReaderDestination()
}
