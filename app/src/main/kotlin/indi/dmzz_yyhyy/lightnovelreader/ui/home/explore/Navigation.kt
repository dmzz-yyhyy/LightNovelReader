package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore

import androidx.compose.animation.ExperimentalSharedTransitionApi
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import io.nightfish.lightnovelreader.api.Route
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded.exploreExpandDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.home.exploreHomeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search.exploreSearchDestination

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavEntryScope.exploreNavigation() {
    exploreHomeDestination()
    exploreExpandDestination()
    exploreSearchDestination()
}

@Suppress("unused")
fun Navigator.navigateToExploreNavigation() {
    navigate(Route.Main.Explore.Home)
}
