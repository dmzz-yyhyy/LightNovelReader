package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToAddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search.navigateToSearchDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.utils.activityHiltViewModel
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.exploreExpandDestination() {
    entry<Route.Main.Explore.Expanded> { entry ->
        val navigator = LocalNavigator.current
        val exploreViewModel = activityHiltViewModel<ExploreViewModel>()
        val viewModel = hiltViewModel<ExpandedPageViewModel>()
        LaunchedEffect(entry.expandedPageDataSourceId) { viewModel.open(entry.expandedPageDataSourceId) }
        ExpandedPageScreen(
            exploreUiState = exploreViewModel.uiState,
            viewModel = viewModel,
            checkConnection = exploreViewModel::refresh,
            onClickBack = { navigator.popBackStack() },
            onClickBook = navigator::navigateToBookDetailDestination,
            onClickExpand = navigator::navigateToExploreExpandDestination,
            onClickSearch = navigator::navigateToSearchDestination,
            addToBookshelf = { navigator.navigateToAddBookToBookshelfDialog(it) }
        )
    }
}

fun Navigator.navigateToExploreExpandDestination(expandedPageDataSourceId: String) {
    navigate(Route.Main.Explore.Expanded(expandedPageDataSourceId))
}
