package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import io.nightfish.lightnovelreader.api.Route
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home.readingHomeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.readingStatsNavigation

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavEntryScope.readingNavigation(sharedTransitionScope: SharedTransitionScope) {
    readingHomeDestination(sharedTransitionScope)
    readingStatsNavigation()
}

@Suppress("unused")
fun Navigator.navigateToHomeReadingDestination() {
    navigate(Route.Main.Reading.Home)
}
