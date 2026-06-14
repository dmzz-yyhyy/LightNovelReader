package indi.dmzz_yyhyy.lightnovelreader.ui.bookmanager

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.bookManager() {
    composable<Route.BookManager> {
        val navController = LocalNavController.current
        val snackbarHostState = LocalSnackbarHost.current
        val viewModel = hiltViewModel<BookManagerViewModel>()
        val uiState = viewModel.localBookManagerUiState
        val clearedItemsText = stringResource(R.string.book_manager_cleared_items)
        LaunchedEffect(viewModel.clearedItemsFlow) {
            viewModel.clearedItemsFlow.collect { count ->
                snackbarHostState.showSnackbar(
                    clearedItemsText.format(count),
                    withDismissAction = true
                )
            }
        }
        uiState.openStorageOverview = {
            navController.navigate(Route.StorageManager)
        }
        uiState.openBookDetailScreen = { id ->
            navController.navigate(Route.Book.Detail(id))
        }
        BookManagerScreen(
            onClickBack = navController::popBackStackIfResumed,
            downloadItemIdList = viewModel.downloadItemIdList,
            bookInformationMap = viewModel.bookInformationMap,
            uiState = uiState,
            loadBookInfo = viewModel::loadBookInfo,
            onClickCancel = viewModel::onClickCancel,
            onClickClearCompleted = viewModel::onClickClearCompleted
        )
    }
}

fun NavController.navigateToDownloadManager() {
    if (!this.isResumed()) return
    navigate(Route.BookManager)
}
