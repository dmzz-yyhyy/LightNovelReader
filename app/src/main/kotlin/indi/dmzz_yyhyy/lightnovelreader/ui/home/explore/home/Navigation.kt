package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded.navigateToExploreExpandDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search.navigateToSearchDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.exploreHomeDestination() {
    composable<Route.Main.Explore.Home> { entry ->
        val navController = LocalNavController.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val exploreViewModel = hiltViewModel<ExploreViewModel>(parentEntry)
        val exploreHomeViewModel = hiltViewModel<ExploreHomeViewModel>()
        if (exploreHomeViewModel.customExplorePageProvider == null) {
            ExploreHomeScreen(
                exploreUiState = exploreViewModel.uiState,
                exploreHomeUiState = exploreHomeViewModel.uiState,
                onClickExpand = navController::navigateToExploreExpandDestination,
                onClickBook = navController::navigateToBookDetailDestination,
                init = exploreHomeViewModel::init,
                changePage = exploreHomeViewModel::changePage,
                onClickSearch = navController::navigateToSearchDestination,
                refresh = exploreHomeViewModel::refresh
            )
        } else {
            CustomExploreHomeScreen(
                init = exploreHomeViewModel::init,
                onClickSearch = navController::navigateToSearchDestination,
                customExplorePageProvider = exploreHomeViewModel.customExplorePageProvider!!
            )
        }
    }
}

fun NavController.navigateToExploreHomeDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Explore.Home)
}