package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation

class IsCompletedSwitchFilter(
    onChange: () -> Unit
): SwitchFilter("已完结", onChange), LocalFilter{
    override fun filter(bookInformation: BookInformation): Boolean =
        !this.enable || bookInformation.isComplete
}