package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.sourcechange

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.settingsSourceChangeDestination() {
    entry<Route.Main.Settings.SourceChange.List> {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<SourceChangeViewModel>()

        SourceChangeScreen(
            uiState = viewModel.uiState,
            onClickBack = navigator::popBackStack,
            onApplyClick = { selectedId ->
                viewModel.changeWebSource(selectedId)
            },
        )
    }
}

fun Navigator.navigateToSettingsSourceChangeDestination() {
    navigate(Route.Main.Settings.SourceChange.List)
}
