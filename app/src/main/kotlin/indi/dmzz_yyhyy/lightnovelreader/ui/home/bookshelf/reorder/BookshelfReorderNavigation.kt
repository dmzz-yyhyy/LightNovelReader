package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.reorder

import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit.navigateToBookshelfEditDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home.BookshelfHomeViewModel
import indi.dmzz_yyhyy.lightnovelreader.utils.activityHiltViewModel
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.bookshelfReorderDestination() {
    entry<Route.Main.Bookshelf.ReorderBooks> { entry ->
        val navigator = LocalNavigator.current
        val bookshelfHomeViewModel = activityHiltViewModel<BookshelfHomeViewModel>()

        BookshelfReorderBooksScreen(
            bookshelfId = entry.id,
            uiState = bookshelfHomeViewModel.uiState,
            prepare = bookshelfHomeViewModel::enableReorderMode,
            onExit = bookshelfHomeViewModel::disableReorderMode,
            moveBook = bookshelfHomeViewModel::moveBook,
            onClickBack = navigator::popBackStack
        )
    }
    entry<Route.Main.Bookshelf.ReorderBookshelves> {
        val navigator = LocalNavigator.current
        val bookshelfHomeViewModel = activityHiltViewModel<BookshelfHomeViewModel>()
        val bookshelfEditTitle = stringResource(R.string.bookshelf_edit_title)

        BookshelfReorderBookshelvesScreen(
            uiState = bookshelfHomeViewModel.uiState,
            prepare = bookshelfHomeViewModel::enableBookshelfReorderMode,
            onExit = bookshelfHomeViewModel::disableBookshelfReorderMode,
            onClickBack = navigator::popBackStack,
            onClickEditBookshelf = {
                navigator.navigateToBookshelfEditDestination(it, bookshelfEditTitle)
            }
        )
    }
}
