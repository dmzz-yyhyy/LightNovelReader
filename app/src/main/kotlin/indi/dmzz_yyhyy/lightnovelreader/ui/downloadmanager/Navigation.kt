package indi.dmzz_yyhyy.lightnovelreader.ui.downloadmanager

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.downloadManager() {
    composable<Route.DownloadManager> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<DownloadManagerViewModel>()
        DownloadManagerScreen(
            downloadItemIdList = viewModel.downloadItemIdList,
            bookInformationMap = viewModel.bookInformationMap,
            loadBookInfo = viewModel::loadBookInfo,
            onClickBack = navController::popBackStackIfResumed,
            onClickCancel = viewModel::onClickCancel,
            onClickClearCompleted = viewModel::onClickClearCompleted
        )
    }
}

fun NavController.navigateToDownloadManager() {
    if (!this.isResumed()) return
    navigate(Route.DownloadManager)
}