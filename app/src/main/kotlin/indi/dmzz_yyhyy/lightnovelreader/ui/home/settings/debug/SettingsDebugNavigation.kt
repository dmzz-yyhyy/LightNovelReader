package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.debug

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.settingsDebugDestination() {
    composable<Route.Main.Settings.Debug> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<DebugScreenViewModel>()
        DebugScreen(
            onClickBack = navController::popBackStackIfResumed,
            onClickQuery = viewModel::runSQLCommand,
            onClickOpenBook = {
                navController.navigateToBookDetailDestination(it)
            },
            result = viewModel.result
        )
    }
}

fun NavController.navigateToSettingsDebugDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Settings.Debug)
}