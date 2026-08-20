package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.detailed

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun NavController.navigateToReadingStatsDetailedDestination(target: Int) {
    if (!this.isResumed()) return
    navigate(Route.Main.Reading.Stats.Detailed(target))
}

fun NavGraphBuilder.readingStatsDetailedDestination() {
    composable<Route.Main.Reading.Stats.Detailed> {
        val navController = LocalNavController.current
        val statsDetailedViewModel = hiltViewModel<StatsDetailedViewModel>()
        val targetDate = it.toRoute<Route.Main.Reading.Stats.Detailed>().targetDate
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val date = LocalDate.parse(targetDate.toString(), formatter)
        statsDetailedViewModel.uiState.selectedDate = date
        StatsDetailedScreen(
            viewModel = statsDetailedViewModel,
            initialize = statsDetailedViewModel::initialize,
            targetDate = date,
            onClickBack = navController::popBackStackIfResumed
        )
    }
}