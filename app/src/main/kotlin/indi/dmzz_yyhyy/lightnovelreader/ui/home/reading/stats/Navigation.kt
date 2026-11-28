package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.readingStatsNavigation() {
    readingStatsOverviewDestination()
}

fun NavEntryScope.readingStatsOverviewDestination() {
    entry<Route.Main.Reading.Stats.Overview> {
        val navigator = LocalNavigator.current
        val statsOverviewViewModel = hiltViewModel<StatsOverviewViewModel>()
        StatsOverviewScreen(
            uiState = statsOverviewViewModel.uiState,
            onClickBack = navigator::popBackStack,
            onClickRetry = statsOverviewViewModel::reloadData,
            onClickDate = statsOverviewViewModel::selectDate
        )
    }
}

fun Navigator.navigateToReadingStatsDestination() {
    navigate(Route.Main.Reading.Stats.Overview)
}
