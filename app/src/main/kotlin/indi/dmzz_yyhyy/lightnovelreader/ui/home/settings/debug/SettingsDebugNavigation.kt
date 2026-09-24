package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.debug

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.settingsDebugDestination() {
    entry<Route.Main.Settings.Debug> {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<DebugScreenViewModel>()
        DebugScreen(
            onClickBack = navigator::popBackStack,
            onClickQuery = viewModel::runSQLCommand,
            onClickOpenBook = {
                navigator.navigateToBookDetailDestination(it)
            },
            result = viewModel.result
        )
    }
}

fun Navigator.navigateToSettingsDebugDestination() {
    navigate(Route.Main.Settings.Debug)
}