package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToAddBookToBookshelfDialog
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.book.RelatedBooksRequest
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed

fun NavGraphBuilder.exploreExpandDestination() {
    composable<Route.Main.Explore.Expanded> { entry ->
        val route = entry.toRoute<Route.Main.Explore.Expanded>()
        ExpandedDestination(entry) { init(route.expandedPageDataSourceId) }
    }
    composable<Route.Main.Explore.RelatedBooks> { entry ->
        val route = entry.toRoute<Route.Main.Explore.RelatedBooks>()
        ExpandedDestination(entry) {
            initRelated(route.sourceId, RelatedBooksRequest(route.bookId, route.kind, route.value))
        }
    }
}

@Composable
private fun ExpandedDestination(entry: NavBackStackEntry, init: ExpandedPageViewModel.() -> Unit) {
    val navController = LocalNavController.current
    val viewModel = hiltViewModel<ExpandedPageViewModel>(entry)
    var dialog: @Composable () -> Unit by remember { mutableStateOf(@Composable {}) }
    ExpandedPageScreen(
        expandedPageUiState = viewModel.uiState,
        dialog = { dialog = it },
        init = { init(viewModel) },
        loadMore = viewModel::loadMore,
        refreshResult = viewModel::loadBookResult,
        requestAddBookToBookshelf = navController::navigateToAddBookToBookshelfDialog,
        onClickBack = { navController.popBackStackIfResumed() },
        onClickBook = navController::navigateToBookDetailDestination
    )
    dialog()
}

fun NavController.navigateToExploreExpandDestination(expandedPageDataSourceId: String) {
    if (!isResumed()) return
    navigate(Route.Main.Explore.Expanded(expandedPageDataSourceId))
}

fun NavController.navigateToRelatedBooksDestination(sourceId: String, request: RelatedBooksRequest) {
    if (!isResumed() || sourceId.isBlank() || request.value.isBlank()) return
    navigate(Route.Main.Explore.RelatedBooks(sourceId, request.bookId, request.kind, request.value))
}
