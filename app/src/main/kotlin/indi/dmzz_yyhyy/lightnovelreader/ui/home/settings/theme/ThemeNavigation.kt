package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.settingsAppThemeDestination() {
    entry<Route.Main.Settings.Theme> {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<ThemeViewModel>()
        ThemeScreen(
            settingState = viewModel.settingState,
            onClickBack = navigator::popBackStack
        )
    }
}

fun Navigator.navigateToSettingsAppThemeDestination() {
    navigate(Route.Main.Settings.Theme)
}
