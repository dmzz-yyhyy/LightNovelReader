package indi.dmzz_yyhyy.lightnovelreader.data.exploration

data class ExplorationPage(
    val title: String,
    val rows: List<ExplorationBooksRow>
) {
    companion object {
        fun empty() = ExplorationPage(
            "",
            emptyList()
        )
    }
}
