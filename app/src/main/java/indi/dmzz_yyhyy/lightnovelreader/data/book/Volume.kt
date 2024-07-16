package indi.dmzz_yyhyy.lightnovelreader.data.book

data class Volume(
    val volumeId: Int,
    val volumeTitle: String,
    val chapters: List<ChapterInformation>
)
