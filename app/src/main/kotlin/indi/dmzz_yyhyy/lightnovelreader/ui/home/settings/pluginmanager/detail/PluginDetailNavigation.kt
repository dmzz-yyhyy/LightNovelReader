package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.detail

import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginManagerViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginSignatureDialog
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.activityHiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.utils.restart
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.settingsPluginManagerDetailDestination() {
    entry<Route.Main.Settings.PluginManager.Detail> { navBackStackEntry ->
        val navigator = LocalNavigator.current
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = LocalSnackbarHost.current
        val context = LocalContext.current
        var showSignatureDialog by remember { mutableStateOf(false) }
        val viewModel = activityHiltViewModel<PluginManagerViewModel>()

        val pluginId = navBackStackEntry.id
        val plugin = viewModel.pluginList.find { it.packageName == pluginId }
        val enabledPluginList by viewModel.enabledPluginFlow.collectAsStateWithLifecycle(emptyList())
        val enabled = enabledPluginList.contains(pluginId)

        val restartToApply = stringResource(R.string.restart_to_apply_changes)
        val restartString = stringResource(R.string.restart)
        PluginDetailScreen(
            isEnabled = enabled,
            pluginInfo = plugin,
            onClickBack = navigator::popBackStack,
            onClickSwitch = { pluginMetadata ->
                viewModel.onClickEnabledSwitch(pluginMetadata)
                showSnackbar(
                    coroutineScope = coroutineScope,
                    hostState = snackbarHostState,
                    message = restartToApply,
                    actionLabel = restartString
                ) {
                    if (it == SnackbarResult.ActionPerformed) {
                        viewModel.unloadAllDisenablePlugin()
                        restart(context)
                    }
                }
            },
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

fun Navigator.navigateToSettingsPluginManagerDetailDestination(id: String) {
    navigate(Route.Main.Settings.PluginManager.Detail(id))
}
