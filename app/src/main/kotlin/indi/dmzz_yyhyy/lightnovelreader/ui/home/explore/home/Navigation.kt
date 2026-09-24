package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded.navigateToExploreExpandDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search.navigateToSearchDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.activityHiltViewModel
import io.nightfish.lightnovelreader.api.Route

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavEntryScope.exploreHomeDestination() {
    entry<Route.Main.Explore.Home> {
        val navigator = LocalNavigator.current
        val exploreViewModel = activityHiltViewModel<ExploreViewModel>()
        val exploreHomeViewModel = hiltViewModel<ExploreHomeViewModel>()
        if (exploreHomeViewModel.customExplorePageProvider == null) {
            ExploreHomeScreen(
                exploreUiState = exploreViewModel.uiState,
                exploreHomeUiState = exploreHomeViewModel.uiState,
                onClickExpand = navigator::navigateToExploreExpandDestination,
                onClickBook = navigator::navigateToBookDetailDestination,
                init = exploreHomeViewModel::init,
                changePage = exploreHomeViewModel::changePage,
                onClickSearch = navigator::navigateToSearchDestination,
                refresh = exploreHomeViewModel::refresh
            )
        } else {
            CustomExploreHomeScreen(
                init = exploreHomeViewModel::init,
                onClickSearch = navigator::navigateToSearchDestination,
                customExplorePageProvider = exploreHomeViewModel.customExplorePageProvider!!
            )
        }
    }
}

@Suppress("unused")
fun Navigator.navigateToExploreHomeDestination() {
    navigate(Route.Main.Explore.Home)
}
