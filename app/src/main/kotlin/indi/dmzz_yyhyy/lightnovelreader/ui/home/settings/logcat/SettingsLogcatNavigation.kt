package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.settingsLogcatDestination() {
    entry<Route.Main.Settings.Logcat> {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<LogcatViewModel>()
        LogcatScreen(
            uiState = viewModel.uiState,
            onClickBack = navigator::popBackStack
        )
    }
}


fun Navigator.navigateToSettingsLogcatDestination() {
    navigate(Route.Main.Settings.Logcat)
}

