package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import io.nightfish.lightnovelreader.api.Route
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit.bookshelfEditDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home.bookshelfHomeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.reorder.bookshelfReorderDestination

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavEntryScope.bookshelfNavigation(sharedTransitionScope: SharedTransitionScope) {
    bookshelfHomeDestination(sharedTransitionScope)
    bookshelfEditDestination()
    bookshelfReorderDestination()
}

@Suppress("unused")
fun Navigator.navigateToBookshelfNavigation() {
    navigate(Route.Main.Bookshelf.Home)
}
