package indi.dmzz_yyhyy.lightnovelreader.data.book

data class Information(val data: Data) {
    data class Data(
        val bookID: Int,
        val bookName: String,
        val bookCoverURL: String,
        val bookIntroduction: String,
    )
}