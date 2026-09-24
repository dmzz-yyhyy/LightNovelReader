package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.settingsLogcatDestination() {
    entry<Route.Main.Settings.Logcat> {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<LogcatViewModel>()
        LifecycleEventEffect(Lifecycle.Event.ON_START) {
            if (!viewModel.uiState.isFileMode) viewModel.startLogging()
        }
        val logEntries by remember { derivedStateOf { viewModel.displayedLogEntries } }
        LogcatScreen(
            uiState = viewModel.uiState,
            logFiles = viewModel.logFilenameList,
            logEntries = logEntries,
            onClickBack = navigator::popBackStack,
            onClickClearLogs = viewModel::clearLogs,
            onClickShareLogs = viewModel::shareLogs,
            onClickDeleteLogFile = viewModel::deleteLogFile,
            onSelectLogFile = viewModel::onSelectLogFile
        )
    }
}


fun Navigator.navigateToSettingsLogcatDestination() {
    navigate(Route.Main.Settings.Logcat)
}

