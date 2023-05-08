package indi.dmzz_yyhyy.lightnovelreader.data

import indi.dmzz_yyhyy.lightnovelreader.tool.MD5

data class BookData(val bookName: String, val coverURL: String, val bookContent: BookContent) {
    val bookUID = bookName.MD5
}
