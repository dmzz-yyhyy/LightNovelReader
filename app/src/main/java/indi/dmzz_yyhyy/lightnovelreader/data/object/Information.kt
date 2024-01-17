package indi.dmzz_yyhyy.lightnovelreader.data.`object`

data class Information(
        val id: Int,
        val name: String,
        val coverUrl: String,
        val writer: String,
        val type: String,
        val tags: List<String>,
        val introduction: String
)