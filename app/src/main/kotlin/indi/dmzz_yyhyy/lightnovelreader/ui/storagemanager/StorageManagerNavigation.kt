package indi.dmzz_yyhyy.lightnovelreader.ui.storagemanager

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.storageManagerDestination() {
    entry<Route.StorageManager> {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<StorageManagerViewModel>()
        StorageManagerScreen(
            onClickBack = navigator::popBackStack,
            uiState = viewModel.uiState
        )
    }
}

fun Navigator.navigateToStorageManager() {
    navigate(Route.StorageManager)
}
