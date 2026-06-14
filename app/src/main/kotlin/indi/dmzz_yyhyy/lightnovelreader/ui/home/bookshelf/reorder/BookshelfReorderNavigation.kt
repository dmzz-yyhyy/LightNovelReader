package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.reorder

import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit.navigateToBookshelfEditDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home.BookshelfHomeViewModel
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.bookshelfReorderDestination() {
    composable<Route.Main.Bookshelf.ReorderBooks> { entry ->
        val navController = LocalNavController.current
        val parentEntry = remember(entry) { navController.getBackStackEntry(Route.Main) }
        val bookshelfHomeViewModel = hiltViewModel<BookshelfHomeViewModel>(parentEntry)
        val route = entry.toRoute<Route.Main.Bookshelf.ReorderBooks>()

        BookshelfReorderBooksScreen(
            bookshelfId = route.id,
            uiState = bookshelfHomeViewModel.uiState,
            prepare = bookshelfHomeViewModel::enableReorderMode,
            onExit = bookshelfHomeViewModel::disableReorderMode,
            getBookInfoFlow = bookshelfHomeViewModel::getBookInfoStateFlow,
            moveBook = bookshelfHomeViewModel::moveBook,
            onClickBack = navController::popBackStack
        )
    }
    composable<Route.Main.Bookshelf.ReorderBookshelves> {
        val navController = LocalNavController.current
        val parentEntry = remember(it) { navController.getBackStackEntry(Route.Main) }
        val bookshelfHomeViewModel = hiltViewModel<BookshelfHomeViewModel>(parentEntry)
        val bookshelfEditTitle = stringResource(R.string.bookshelf_edit_title)

        BookshelfReorderBookshelvesScreen(
            uiState = bookshelfHomeViewModel.uiState,
            prepare = bookshelfHomeViewModel::enableBookshelfReorderMode,
            onExit = bookshelfHomeViewModel::disableBookshelfReorderMode,
            onClickBack = navController::popBackStack,
            onClickEditBookshelf = {
                navController.navigateToBookshelfEditDestination(it, bookshelfEditTitle)
            }
        )
    }
}
