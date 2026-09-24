package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToAddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreViewModel
import indi.dmzz_yyhyy.lightnovelreader.utils.activityHiltViewModel
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.exploreExpandDestination() {
    entry<Route.Main.Explore.Expanded> { entry ->
        val navigator = LocalNavigator.current
        val exploreViewModel = activityHiltViewModel<ExploreViewModel>()
        val exploreExpandedPageHomeViewModel = hiltViewModel<ExpandedPageViewModel>()
        var dialog: @Composable () -> Unit by remember { mutableStateOf(@Composable {}) }
        ExpandedPageScreen(
            exploreUiState = exploreViewModel.uiState,
            expandedPageUiState = exploreExpandedPageHomeViewModel.uiState,
            refresh = exploreViewModel::refresh,
            dialog = { newDialog -> dialog = newDialog },
            expandedPageDataSourceId = entry.expandedPageDataSourceId,
            init = exploreExpandedPageHomeViewModel::init,
            loadMore = exploreExpandedPageHomeViewModel::loadMore,
            refreshResult = exploreExpandedPageHomeViewModel::loadBookResult,
            requestAddBookToBookshelf = {
                navigator.navigateToAddBookToBookshelfDialog(it)
            },
            onClickBack = {
                exploreExpandedPageHomeViewModel.clear()
                navigator.popBackStack()
            },
            onClickBook = {
                navigator.navigateToBookDetailDestination(it)
            }
        )
        dialog.invoke()
    }
}

fun Navigator.navigateToExploreExpandDestination(expandedPageDataSourceId: String) {
    navigate(Route.Main.Explore.Expanded(expandedPageDataSourceId))
}