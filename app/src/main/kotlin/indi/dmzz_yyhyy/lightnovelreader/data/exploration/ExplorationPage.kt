package indi.dmzz_yyhyy.lightnovelreader.data.exploration

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class ExplorationPage(
    val title: String,
    val rows: Flow<List<ExplorationBooksRow>>
) {
    companion object {
        fun empty() = ExplorationPage(
            "",
            flowOf(emptyList())
        )
    }
}
