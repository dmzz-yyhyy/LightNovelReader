package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.settingsAppThemeDestination() {
    composable<Route.Main.Settings.Theme> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<ThemeViewModel>()
        ThemeScreen(
            settingState = viewModel.settingState,
            onClickBack = navController::popBackStackIfResumed
        )
    }
}

fun NavController.navigateToSettingsAppThemeDestination() {
    navigate(Route.Main.Settings.Theme)
}
