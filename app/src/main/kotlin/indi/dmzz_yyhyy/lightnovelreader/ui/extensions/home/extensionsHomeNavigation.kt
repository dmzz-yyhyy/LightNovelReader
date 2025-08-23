package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.extensionsHomeNavigation(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Extensions.Home> {
        val navController = LocalNavController.current
        ExtensionsHomeScreen(
            onNavigateToRepositories = {
                navController.navigate(Route.Main.Extensions.Repositories)
            },
            onNavigateToBrowse = {
                navController.navigate(Route.Main.Extensions.Browse)
            },
            onNavigateToInstalled = {
                navController.navigate(Route.Main.Extensions.Installed)
            }
        )
    }
}
