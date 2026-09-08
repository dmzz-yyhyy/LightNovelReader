package indi.dmzz_yyhyy.lightnovelreader.ui.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.bookshelfNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.exploreNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.readingNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.settingsNavigation
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import io.nightfish.lightnovelreader.api.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavEntryScope.homeNavigation(sharedTransitionScope: SharedTransitionScope) {
    readingNavigation(sharedTransitionScope)
    exploreNavigation()
    bookshelfNavigation(sharedTransitionScope)
    settingsNavigation()
}

@Suppress("unused")
fun Navigator.navigateToHomeNavigation() {
    navigate(Route.Main.Reading.Home)
}
