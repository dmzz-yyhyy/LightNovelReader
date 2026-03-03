package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.applist

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginManagerViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.settingsPluginAppListDestination() {
    composable<Route.Main.Settings.PluginManager.AppList> { navBackStackEntry ->
        val navController = LocalNavController.current
        val parentEntry = remember(navBackStackEntry) {
            navBackStackEntry.destination.parent?.route
                ?.let(navController::getBackStackEntry)
        }
        val viewModel = hiltViewModel<PluginManagerViewModel>(parentEntry ?: navBackStackEntry)
        PluginAppListScreen(
            appPluginList = viewModel.scannedPluginApps,
            onRefresh = {},
            onClickBack = navController::popBackStackIfResumed
        )
    }
}

fun NavController.navigateToSettingsPluginAppListDestination() {
    navigate(Route.Main.Settings.PluginManager.AppList)
}
