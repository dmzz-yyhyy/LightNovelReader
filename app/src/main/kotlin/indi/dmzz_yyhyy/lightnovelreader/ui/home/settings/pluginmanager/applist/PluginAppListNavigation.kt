package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.applist

import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginManagerViewModel
import indi.dmzz_yyhyy.lightnovelreader.utils.activityHiltViewModel
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.settingsPluginAppListDestination() {
    entry<Route.Main.Settings.PluginManager.AppList> {
        val navigator = LocalNavigator.current
        val viewModel = activityHiltViewModel<PluginManagerViewModel>()
        PluginAppListScreen(
            appPluginList = viewModel.scannedPluginApps,
            onRefresh = {},
            onClickBack = navigator::popBackStack
        )
    }
}

fun Navigator.navigateToSettingsPluginAppListDestination() {
    navigate(Route.Main.Settings.PluginManager.AppList)
}
