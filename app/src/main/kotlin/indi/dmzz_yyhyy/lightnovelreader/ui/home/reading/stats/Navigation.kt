package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.navigateToReadingStatsDetailedDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed.readingStatsDetailedDestination
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.readingStatsNavigation() {
    readingStatsOverviewDestination()
    readingStatsDetailedDestination()
}

fun NavEntryScope.readingStatsOverviewDestination() {
    entry<Route.Main.Reading.Stats.Overview> {
        val navigator = LocalNavigator.current
        val statsOverviewViewModel = hiltViewModel<StatsOverviewViewModel>()
        StatsOverviewScreen(
            onClickBack = navigator::popBackStack,
            viewModel = statsOverviewViewModel,
            onClickDetailScreen = navigator::navigateToReadingStatsDetailedDestination
        )
    }
}

fun Navigator.navigateToReadingStatsDestination() {
    navigate(Route.Main.Reading.Stats.Overview)
}
