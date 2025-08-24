package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.exploration

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.extensionExplorationNavigation(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Extensions.Exploration> {
        val navController = LocalNavController.current
        ExtensionExplorationHomeScreen(
            onBack = { navController.popBackStack() },
            onNavigateToSearch = { navController.navigate(Route.Main.Extensions.Search) },
            onNavigateToExtension = { extensionId ->
                // Navigate to search screen and browse the selected extension
                navController.navigate(Route.Main.Extensions.Search)
                // TODO: Pass extension ID to search screen for browsing
            }
        )
    }

    composable<Route.Main.Extensions.Search> {
        val navController = LocalNavController.current
        ExtensionSearchScreen(
            onBack = { navController.popBackStack() },
            onBookClick = { book ->
                // Navigate to existing book detail screen
                // The book ID should be constructed to include extension info
                navController.navigateToBookDetailDestination(book.id)
            }
        )
    }
}
