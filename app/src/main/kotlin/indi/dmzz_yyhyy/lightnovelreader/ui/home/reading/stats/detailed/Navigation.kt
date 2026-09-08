package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import io.nightfish.lightnovelreader.api.Route
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Navigator.navigateToReadingStatsDetailedDestination(target: Int) {
    navigate(Route.Main.Reading.Stats.Detailed(target))
}

fun NavEntryScope.readingStatsDetailedDestination() {
    entry<Route.Main.Reading.Stats.Detailed> {
        val navigator = LocalNavigator.current
        val statsDetailedViewModel = hiltViewModel<StatsDetailedViewModel>()
        val targetDate = it.targetDate
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val date = LocalDate.parse(targetDate.toString(), formatter)
        statsDetailedViewModel.uiState.selectedDate = date
        StatsDetailedScreen(
            viewModel = statsDetailedViewModel,
            initialize = statsDetailedViewModel::initialize,
            targetDate = date,
            onClickBack = navigator::popBackStack
        )
    }
}