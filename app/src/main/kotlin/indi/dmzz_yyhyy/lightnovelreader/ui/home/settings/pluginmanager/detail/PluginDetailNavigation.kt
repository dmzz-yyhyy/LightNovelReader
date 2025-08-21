package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginManagerViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.settingsPluginManagerDetailDestination() {
    composable<Route.Main.Settings.PluginManager.Detail> { navBackStackEntry ->
        val navController = LocalNavController.current

        val viewModel = hiltViewModel<PluginManagerViewModel>()
        val pluginId = navBackStackEntry.toRoute<Route.Main.Settings.PluginManager.Detail>().id
        val plugin = viewModel.pluginList.find { it.id == pluginId }

        PluginDetailScreen(
            plugin = plugin,
            onClickBack = navController::popBackStackIfResumed,
            onClickSwitch = viewModel::onClickEnabledSwitch
        )
    }
}

fun NavController.navigateToSettingsPluginManagerDetailDestination(id: String) {
    navigate(Route.Main.Settings.PluginManager.Detail(id))
}
