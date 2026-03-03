package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.DeleteProgressDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.InstallProgressDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.PluginDialogMode
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.UpdateCheckDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.pluginInstallerDialog() {
    dialog<Route.PluginInstallerDialog> { entry ->
        val snackbarHostState = LocalSnackbarHost.current
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<PluginInstallerDialogViewModel>()
        val route = entry.toRoute<Route.PluginInstallerDialog>()
        val source = route.source

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
            if (uiState.closeSignal > 0) navController.popBackStack()
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

fun NavController.navigateToPluginInstallerDialog(string: String) {
    navigate(Route.PluginInstallerDialog(string))
}
