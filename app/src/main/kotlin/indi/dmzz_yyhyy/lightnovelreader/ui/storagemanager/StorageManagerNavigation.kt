package indi.dmzz_yyhyy.lightnovelreader.ui.storagemanager

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import io.nightfish.lightnovelreader.api.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.storageManager() {
    composable<Route.StorageManager> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<StorageManagerViewModel>()
        StorageManagerScreen(
            onClickBack = navController::popBackStackIfResumed,
            uiState = viewModel.uiState
        )
    }
}

fun NavController.navigateToStorageManager() {
    if (!this.isResumed()) return
    navigate(Route.StorageManager)
}
