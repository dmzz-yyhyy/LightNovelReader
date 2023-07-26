package indi.dmzz_yyhyy.lightnovelreader.data.book

data class Content(
    var data: DataBean,
) {
    data class DataBean(
        var title: String,
        var content: String,
    )
}