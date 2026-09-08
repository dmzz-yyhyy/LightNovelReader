package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.formats

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.settingsFormatsDestination() {
    entry<Route.Main.Settings.Formats> {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<FormatsViewModel>()
        val settingState = viewModel.settingState
        FormatsScreen(
            settingState = settingState,
            onClickBack = navigator::popBackStack
        )
    }
}

fun Navigator.navigateToSettingsFormatsDestination() {
    navigate(Route.Main.Settings.Formats)
}
