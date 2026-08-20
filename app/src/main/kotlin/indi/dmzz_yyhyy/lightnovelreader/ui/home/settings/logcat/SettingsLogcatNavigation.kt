package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.settingsLogcatDestination() {
    composable<Route.Main.Settings.Logcat> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<LogcatViewModel>()
        LifecycleEventEffect(Lifecycle.Event.ON_START) {
            if (!viewModel.uiState.isFileMode) viewModel.startLogging()
        }
        val logEntries by remember { derivedStateOf { viewModel.displayedLogEntries } }
        LogcatScreen(
            uiState = viewModel.uiState,
            logFiles = viewModel.logFilenameList,
            logEntries = logEntries,
            onClickBack = navController::popBackStackIfResumed,
            onClickClearLogs = viewModel::clearLogs,
            onClickShareLogs = viewModel::shareLogs,
            onClickDeleteLogFile = viewModel::deleteLogFile,
            onSelectLogFile = viewModel::onSelectLogFile
        )
    }
}


fun NavController.navigateToSettingsLogcatDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Settings.Logcat)
}

