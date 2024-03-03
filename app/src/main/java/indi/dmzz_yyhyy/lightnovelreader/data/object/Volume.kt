package indi.dmzz_yyhyy.lightnovelreader.data.`object`

data class Volume(
    var volumeTitle: String,
    var chapters: List<Chapter>,
) {
    data class Chapter(
        val title: String,
        val id: Int,
    )
}