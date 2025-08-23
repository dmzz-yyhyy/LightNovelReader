package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.extensionsHomeNavigation(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Extensions.Home> {
        ExtensionsHomeScreen(
            onNavigateToRepositories = {
                // Navigation will be handled by parent
            },
            onNavigateToBrowse = {
                // Navigation will be handled by parent
            },
            onNavigateToInstalled = {
                // Navigation will be handled by parent
            }
        )
    }
}
