package indi.dmzz_yyhyy.lightnovelreader.ui.bookmanager

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.bookManagerDestination() {
    entry<Route.BookManager> {
        val navigator = LocalNavigator.current
        val snackbarHostState = LocalSnackbarHost.current
        val viewModel = hiltViewModel<BookManagerViewModel>()
        val uiState = viewModel.localBookManagerUiState
        val resources = LocalResources.current
        LaunchedEffect(viewModel.clearedItemsFlow) {
            viewModel.clearedItemsFlow.collect { count ->
                snackbarHostState.showSnackbar(
                    resources.getQuantityString(
                        R.plurals.book_manager_cleared_items,
                        count,
                        count
                    ),
                    withDismissAction = true
                )
            }
        }
        uiState.openStorageOverview = {
            navigator.navigate(Route.StorageManager)
        }
        uiState.openBookDetailScreen = { id ->
            navigator.navigate(Route.Book.Detail(id))
        }
        BookManagerScreen(
            onClickBack = navigator::popBackStack,
            downloadItemIdList = viewModel.downloadItemIdList,
            uiState = uiState,
            onClickCancel = viewModel::onClickCancel,
            onClickClearCompleted = viewModel::onClickClearCompleted
        )
    }
}

fun Navigator.navigateToDownloadManager() {
    navigate(Route.BookManager)
}
