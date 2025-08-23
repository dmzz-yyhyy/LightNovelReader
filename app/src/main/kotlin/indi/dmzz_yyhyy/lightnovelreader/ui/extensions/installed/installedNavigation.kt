package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.installed

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.installedNavigation(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Extensions.Installed> {
        InstalledScreen(
            onBack = {
                // Navigation will be handled by parent
            }
        )
    }
}
