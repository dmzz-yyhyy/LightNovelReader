package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
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
        SearchDestination(entry)
    }
    composable<Route.Main.Explore.AuthorSearch> { entry ->
        SearchDestination(entry, entry.toRoute<Route.Main.Explore.AuthorSearch>().author)
    }
}

@Composable
private fun SearchDestination(entry: NavBackStackEntry, author: String? = null) {
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
        init = { exploreSearchViewModel.init(author, navController::navigateToBookDetailDestination) },
        onChangeSearchType = { exploreSearchViewModel.changeSearchType(it) },
        onSearch = { exploreSearchViewModel.search(it, navController::navigateToBookDetailDestination) },
        onClickDeleteHistory = { exploreSearchViewModel.deleteHistory(it) },
        onClickClearAllHistory = exploreSearchViewModel::clearAllHistory,
        onClickBook = {
            navController.navigateToBookDetailDestination(it)
        },
        onKeywordChange = exploreSearchViewModel::updateKeyword
    )
}

fun NavController.navigateToAuthorSearchDestination(author: String) {
    if (!isResumed() || author.isBlank()) return
    navigate(Route.Main.Explore.AuthorSearch(author.trim()))
}

fun NavController.navigateToSearchDestination() {
    if (!this.isResumed()) return
    navigate(Route.Main.Explore.Search)
}