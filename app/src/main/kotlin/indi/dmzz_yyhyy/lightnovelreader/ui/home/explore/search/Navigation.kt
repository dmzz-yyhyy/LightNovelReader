package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToAddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreViewModel
import indi.dmzz_yyhyy.lightnovelreader.utils.activityHiltViewModel
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.exploreSearchDestination() {
    entry<Route.Main.Explore.Search> {
        val navigator = LocalNavigator.current
        val exploreViewModel = activityHiltViewModel<ExploreViewModel>()
        val exploreSearchViewModel = hiltViewModel<ExploreSearchViewModel>()
        ExploreSearchScreen(
            exploreUiState = exploreViewModel.uiState,
            exploreSearchUiState = exploreSearchViewModel.uiState,
            refresh = exploreViewModel::refresh,
            requestAddBookToBookshelf = {
                navigator.navigateToAddBookToBookshelfDialog(it)
            },
            onClickBack = navigator::popBackStack,
            init = exploreSearchViewModel::init,
            onChangeSearchType = { exploreSearchViewModel.changeSearchType(it) },
            onSearch = {
                exploreSearchViewModel.search(
                    it,
                    navigator::navigateToBookDetailDestination
                )
            },
            onClickDeleteHistory = { exploreSearchViewModel.deleteHistory(it) },
            onClickClearAllHistory = exploreSearchViewModel::clearAllHistory,
            onClickBook = {
                navigator.navigateToBookDetailDestination(it)
            },
            updateSuggestions = exploreSearchViewModel::updateSuggestions
        )
    }
}

fun Navigator.navigateToSearchDestination() {
    navigate(Route.Main.Explore.Search)
}