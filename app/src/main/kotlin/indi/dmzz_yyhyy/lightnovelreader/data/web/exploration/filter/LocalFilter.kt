package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation

interface LocalFilter {
    fun filter(bookInformation: BookInformation): Boolean
}