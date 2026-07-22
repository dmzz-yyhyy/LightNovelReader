package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.ChapterSelectionBottomSheet
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToBookReaderDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.bookmanager.navigateToDownloadManager
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.navigateToReadingStatsDestination
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
fun NavGraphBuilder.readingHomeDestination(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Reading.Home> { entry ->
        val navController = LocalNavController.current
        val context = LocalContext.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val viewModel = hiltViewModel<ReadingHomeViewModel>(parentEntry)
        val chapterSheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)

        ReadingScreen(
            recentReadingBooks = viewModel.recentReadingBooks,
            onClickDownloadManager = navController::navigateToDownloadManager,
            onClickBook = navController::navigateToBookDetailDestination,
            onClickContinueReading = { bookId, chapterId ->
                navController.navigateToBookDetailDestination(bookId)
                navController.navigateToBookReaderDestination(bookId, chapterId, context)
            },
            sharedTransitionScope = sharedTransitionScope,
            onClickStats = navController::navigateToReadingStatsDestination,
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
                        navController.navigateToBookDetailDestination(chapterSheetUi.bookId)
                        navController.navigateToBookReaderDestination(
                            chapterSheetUi.bookId,
                            chapterId,
                            context
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
