package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToAddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreViewModel
import io.nightfish.lightnovelreader.api.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.exploreSearchDestination() {
    composable<Route.Main.Explore.Search> { entry ->
        val navController = LocalNavController.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val exploreViewModel = hiltViewModel<ExploreViewModel>(parentEntry)
        val exploreSearchViewModel = hiltViewModel<ExploreSearchViewModel>()
        ExploreSearchScreen(
            exploreUiState = exploreViewModel.uiState,
            exploreSearchUiState = exploreSearchViewModel.uiState,
            refresh = exploreViewModel::refresh,
            requestAddBookToBookshelf = {
                navController.navigateToAddBookToBookshelfDialog(it)
            },
            onClickBack = { navController.popBackStackIfResumed() },
            init = exploreSearchViewModel::init,
            onChangeSearchType = { exploreSearchViewModel.changeSearchType(it) },
            onSearch = { exploreSearchViewModel.search(it, navController::navigateToBookDetailDestination) },
            onClickDeleteHistory = { exploreSearchViewModel.deleteHistory(it) },
            onClickClearAllHistory = exploreSearchViewModel::clearAllHistory,
            onClickBook = {
                navController.navigateToBookDetailDestination(it)
            },
            updateSuggestions = exploreSearchViewModel::updateSuggestions
        )
    }
}

fun NavController.navigateToSearchDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Explore.Search)
}