package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.formats

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.settingsFormatsDestination() {
    composable<Route.Main.Settings.Formats> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<FormatsViewModel>(it)
        val settingState = viewModel.settingState
        FormatsScreen(
            settingState = settingState,
            onClickBack = navController::popBackStackIfResumed
        )
    }
}

fun NavController.navigateToSettingsFormatsDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Settings.Formats)
}