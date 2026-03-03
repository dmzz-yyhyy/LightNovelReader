package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager.repository

import androidx.compose.runtime.LaunchedEffect
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToPluginInstallerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.settingsPluginRepositoryDestination() {
    composable<Route.Main.Settings.PluginManager.Repository> { navBackStackEntry ->
        val navController = LocalNavController.current

        val viewModel = hiltViewModel<PluginRepositoryViewModel>()
        val uiState = viewModel.repositoryUiState

        LaunchedEffect(Unit) {
            viewModel.navigateToInstallDialog.collect { file ->
                navController.navigateToPluginInstallerDialog(file.toUri().toString())
            }
        }

        PluginRepositoryScreen(
            installedPluginList = viewModel.pluginList,
            onRefresh = viewModel::loadPluginRepository,
            uiState = uiState,
            onClickBack = navController::popBackStackIfResumed,
            onClickInstallFromRepo = viewModel::enqueueInstallFromRepository,
            onClickCancel = viewModel::cancelQueuedInstall,
            onClickSetRepoUrl = {},
            loadPluginMetadata = viewModel::loadPluginMetadataIfNeeded
        )

        LaunchedEffect(Unit) {
            if (uiState.remotePluginMetadataList.isEmpty() && !uiState.isLoading) {
                viewModel.loadPluginRepository()
            }
        }
    }
}

fun NavController.navigateToSettingsPluginRepositoryDestination() {
    navigate(Route.Main.Settings.PluginManager.Repository)
}