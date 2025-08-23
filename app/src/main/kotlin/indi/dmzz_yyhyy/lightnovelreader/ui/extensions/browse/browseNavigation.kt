package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.browse

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.browseNavigation(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Extensions.Browse> {
        BrowseScreen(
            onBack = {
                // Navigation will be handled by parent
            }
        )
    }
}
