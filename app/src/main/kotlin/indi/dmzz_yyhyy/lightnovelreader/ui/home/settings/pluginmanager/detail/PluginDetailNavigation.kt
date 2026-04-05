package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginManagerViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginSignatureDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.settingsPluginManagerDetailDestination() {
    composable<Route.Main.Settings.PluginManager.Detail> { navBackStackEntry ->
        val navController = LocalNavController.current
        val parentEntry = remember(navBackStackEntry) {
            navBackStackEntry.destination.parent?.route
                ?.let(navController::getBackStackEntry)
        }
        var showSignatureDialog by remember { mutableStateOf(false) }
        val viewModel = hiltViewModel<PluginManagerViewModel>(parentEntry ?: navBackStackEntry)

        val pluginId = navBackStackEntry.toRoute<Route.Main.Settings.PluginManager.Detail>().id
        val plugin = viewModel.pluginList.find { it.packageName == pluginId }
        val enabledPluginList by viewModel.enabledPluginFlow.collectAsState(emptyList())
        val enabled = enabledPluginList.contains(pluginId)
        PluginDetailScreen(
            isEnabled = enabled,
            pluginInfo = plugin,
            onClickBack = navController::popBackStackIfResumed,
            onClickSwitch = viewModel::onClickEnabledSwitch,
            pluginContent = { viewModel.PluginContent(pluginId, it) },
            onClickSignature = {
                showSignatureDialog = true
            }
        )

        if (showSignatureDialog) {
            PluginSignatureDialog(
                onClose = { showSignatureDialog = false },
                signatureInfo = viewModel.getPluginSignatures(pluginId)
            )
        }
    }
}

fun NavController.navigateToSettingsPluginManagerDetailDestination(id: String) {
    navigate(Route.Main.Settings.PluginManager.Detail(id))
}
