package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.data.RouteConfig
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume

data class ChapterUiState(
    val bookName: String = "",
    val bookCoverUrl: String = "",
    val bookIntroduction: String = "",
    val volumeList: List<Volume> = listOf(),

    val onChapterClick: (NavController, ChapterViewModel, Int) -> Unit = {
            navController: NavController,
            chapterViewMode: ChapterViewModel,
            chapterId: Int ->
        chapterViewMode.setChapterContentId(chapterId)
        navController.navigate(RouteConfig.READER)
    }
)
