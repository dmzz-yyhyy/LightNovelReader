package indi.dmzz_yyhyy.lightnovelreader.data.book

import indi.dmzz_yyhyy.lightnovelreader.tool.MD5

data class Book(val bookName: String, val coverURL: String, val bookContent: BookContent) {
    val bookUID = bookName.MD5
}
