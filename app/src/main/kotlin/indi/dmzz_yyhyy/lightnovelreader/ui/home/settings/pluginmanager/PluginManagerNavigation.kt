package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail.navigateToSettingsPluginManagerDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail.settingsPluginManagerDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.settingsPluginManagerNavigation() {
    navigation<Route.Main.Settings.PluginManager>(
        startDestination = Route.Main.Settings.PluginManager.Home
    ) {
        settingsPluginManagerHomeDestination()
        settingsPluginManagerDetailDestination()
    }
}


fun NavGraphBuilder.settingsPluginManagerHomeDestination() {
    composable<Route.Main.Settings.PluginManager.Home> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<PluginManagerViewModel>()
        PluginManagerScreen(
            onClickBack = navController::popBackStackIfResumed,
            onClickDetail = navController::navigateToSettingsPluginManagerDetailDestination,
            pluginList = viewModel.pluginList
        )
    }
}

fun NavController.navigateToSettingsPluginManagerHomeDestination() {
    navigate(Route.Main.Settings.PluginManager.Home)
}
