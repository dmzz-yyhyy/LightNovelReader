package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.overlay.overlayEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.DeleteProgressDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallProgressDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginDialogMode
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.UpdateCheckDialog
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.pluginInstallerDialog() {
    overlayEntry<Route.PluginInstallerDialog> { entry ->
        val snackbarHostState = LocalSnackbarHost.current
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<PluginInstallerDialogViewModel>()
        val source = entry.source

        LaunchedEffect(source) {
            if (source.isNotBlank()) viewModel.setSource(source)
        }
        LaunchedEffect(viewModel) {
            viewModel.snackbarFlow.collect { message ->
                snackbarHostState.showSnackbar(message, withDismissAction = true)
            }
        }
        val uiState = viewModel.uiState

        LaunchedEffect(uiState.closeSignal) {
            if (uiState.closeSignal > 0) navigator.popBackStack()
        }
        when (uiState.mode) {
            PluginDialogMode.Install -> {
                InstallProgressDialog(
                    uiState = uiState,
                    onClickClose = { viewModel.onCancelOperation() },
                    onConfirmDecision = { confirm -> viewModel.respondUserDecision(confirm) }
                )
            }

            PluginDialogMode.Uninstall -> {
                DeleteProgressDialog(
                    uiState = uiState,
                    onClose = { viewModel.onCloseDialog() },
                    onConfirmDelete = { viewModel.confirmDelete() }
                )
            }

            PluginDialogMode.UpdateCheck -> {
                UpdateCheckDialog(
                    uiState = uiState,
                    onClose = { viewModel.onCloseDialog() },
                    onConfirmUpdate = { _ -> viewModel.respondUserDecision(true) }
                )
            }

            PluginDialogMode.Hidden -> Unit
        }
    }
}

fun Navigator.navigateToPluginInstallerDialog(string: String) {
    navigate(Route.PluginInstallerDialog(string))
}
