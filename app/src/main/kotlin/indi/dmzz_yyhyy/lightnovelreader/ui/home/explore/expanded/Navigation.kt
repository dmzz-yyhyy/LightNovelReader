package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToAddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.book.RelatedBooksRequest
import java.util.UUID

fun NavEntryScope.exploreExpandDestination() {
    entry<Route.Main.Explore.Expanded> { route ->
        ExpandedDestination { init(route.expandedPageDataSourceId) }
    }
    entry<Route.Main.Explore.RelatedBooks> { route ->
        ExpandedDestination {
            initRelated(route.sourceId, RelatedBooksRequest(route.bookId, route.kind, route.value))
        }
    }
}

@Composable
private fun ExpandedDestination(init: ExpandedPageViewModel.() -> Unit) {
    val navigator = LocalNavigator.current
    val viewModel = hiltViewModel<ExpandedPageViewModel>()
    var dialog: @Composable () -> Unit by remember { mutableStateOf(@Composable {}) }
    ExpandedPageScreen(
        expandedPageUiState = viewModel.uiState,
        dialog = { dialog = it },
        init = { init(viewModel) },
        loadMore = viewModel::loadMore,
        refreshResult = viewModel::loadBookResult,
        requestAddBookToBookshelf = navigator::navigateToAddBookToBookshelfDialog,
        onClickBack = { navigator.popBackStack() },
        onClickBook = navigator::navigateToBookDetailDestination
    )
    dialog()
}

fun Navigator.navigateToExploreExpandDestination(expandedPageDataSourceId: String) {
    navigate(Route.Main.Explore.Expanded(expandedPageDataSourceId))
}

fun Navigator.navigateToRelatedBooksDestination(sourceId: String, request: RelatedBooksRequest) {
    if (sourceId.isBlank() || request.value.isBlank()) return
    navigate(
        Route.Main.Explore.RelatedBooks(
            sourceId = sourceId,
            bookId = request.bookId,
            kind = request.kind,
            value = request.value,
            entryId = UUID.randomUUID().toString()
        )
    )
}
