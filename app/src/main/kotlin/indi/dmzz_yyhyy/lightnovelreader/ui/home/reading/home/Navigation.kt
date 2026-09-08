package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.ChapterSelectionBottomSheet
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToBookReaderDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.bookmanager.navigateToDownloadManager
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.navigateToReadingStatsDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.activityHiltViewModel
import io.nightfish.lightnovelreader.api.Route

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
fun NavEntryScope.readingHomeDestination(sharedTransitionScope: SharedTransitionScope) {
    entry<Route.Main.Reading.Home> {
        val navigator = LocalNavigator.current
        val viewModel = activityHiltViewModel<ReadingHomeViewModel>()
        val chapterSheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

        ReadingScreen(
            recentReadingBooks = viewModel.recentReadingBooks,
            onClickDownloadManager = navigator::navigateToDownloadManager,
            onClickBook = navigator::navigateToBookDetailDestination,
            onClickContinueReading = { bookId, chapterId ->
                navigator.navigateToBookDetailDestination(bookId)
                navigator.navigateToBookReaderDestination(bookId, chapterId)
            },
            sharedTransitionScope = sharedTransitionScope,
            onClickStats = navigator::navigateToReadingStatsDestination,
            onRemoveBook = viewModel::removeFromReadingList,
            onClickOpenChapters = viewModel::openChapters,
            onAddBook = viewModel::addToReadingList
        )

        viewModel.chapterSheetUiState?.let { chapterSheetUi ->
            val result by chapterSheetUi.bookVolumeFlow.collectAsStateWithLifecycle(null)
            result?.onOk {
                ChapterSelectionBottomSheet(
                    sheetState = chapterSheetState,
                    selectedVolumeId = chapterSheetUi.selectedVolumeId,
                    bookVolumes = it,
                    readingChapterId = chapterSheetUi.readingChapterId,
                    onDismissRequest = viewModel::closeContents,
                    onClickChapter = { chapterId ->
                        navigator.navigateToBookDetailDestination(chapterSheetUi.bookId)
                        navigator.navigateToBookReaderDestination(
                            chapterSheetUi.bookId,
                            chapterId,
                        )
                        viewModel.closeContents()
                    },
                    onChangeSelectedVolumeId = viewModel::setVolume
                )
            }?.onErr {
                //TODO 错误显示
            } ?: {
                //TODO 加载显示
            }
        }
    }
}
