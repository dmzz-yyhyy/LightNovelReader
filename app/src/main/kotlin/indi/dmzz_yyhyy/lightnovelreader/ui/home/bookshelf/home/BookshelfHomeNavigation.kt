package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.overlay.overlayEntry
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.AddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit.navigateToBookshelfEditDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.activityHiltViewModel
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
fun NavEntryScope.bookshelfHomeDestination(sharedTransitionScope: SharedTransitionScope) {
    entry<Route.Main.Bookshelf.Home> {
        val navigator = LocalNavigator.current
        val bookshelfHomeViewModel = activityHiltViewModel<BookshelfHomeViewModel>()
        val bookshelfNewTitle = stringResource(R.string.bookshelf_new_title)
        val bookshelfEditTitle = stringResource(R.string.bookshelf_edit_title)
        val uiState =
            remember(navigator, bookshelfHomeViewModel, bookshelfNewTitle, bookshelfEditTitle) {
                object : BookshelfHomeUiState by bookshelfHomeViewModel.uiState {
                    override val enableReorderMode: () -> Unit = {
                        navigator.navigate(
                            Route.Main.Bookshelf.ReorderBooks(
                                bookshelfHomeViewModel.uiState.selectedBookshelfId
                            )
                        )
                    }
                    override val enableBookshelfReorderMode: () -> Unit = {
                        navigator.navigate(Route.Main.Bookshelf.ReorderBookshelves)
                    }
                    override val onCreate: () -> Unit = {
                        navigator.navigateToBookshelfEditDestination(-1, bookshelfNewTitle)
                    }
                    override val onEdit: (Int) -> Unit = { bookshelfId ->
                        navigator.navigateToBookshelfEditDestination(
                            bookshelfId,
                            bookshelfEditTitle
                        )
                    }
                    override val onBookClick: (String) -> Unit =
                        navigator::navigateToBookDetailDestination
                    override val onRemove: () -> Unit = {
                        bookshelfHomeViewModel.removeSelectedBooks()
                        if (bookshelfHomeViewModel.uiState.selectedBookshelf?.allBookFlows?.isEmpty() == true) {
                            bookshelfHomeViewModel.disableSelectMode()
                        }
                    }
                    override val onMarkSelectedBooks: () -> Unit = {
                        navigator.navigateToAddBookToBookshelfDialog(bookshelfHomeViewModel.uiState.selectedBookIds)
                        bookshelfHomeViewModel.disableSelectMode()
                    }
                }
            }
        BookshelfHomeScreen(
            init = bookshelfHomeViewModel::load,
            uiState = uiState
        )
    }

    addBookToBookshelfDialog()
}

@Suppress("unused")
fun Navigator.navigateToBookshelfHomeDestination() {
    navigate(Route.Main.Bookshelf.Home)
}

private fun NavEntryScope.addBookToBookshelfDialog() {
    overlayEntry<Route.Main.Bookshelf.AddBookToBookshelfDialog> { entry ->
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<AddBookToBookshelfDialogViewModel>()
        val dialogSelectedBookshelves = remember { mutableStateListOf<Int>() }
        val allBookshelves by viewModel.allBookshelfFlow.collectAsStateWithLifecycle(emptyList<Bookshelf>())
        AddBookToBookshelfDialog(
            onDismissRequest = { navigator.popBackStack() },
            onConfirmation = {
                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.markSelectedBooks(entry.selectedBookIds, dialogSelectedBookshelves)
                }
                navigator.popBackStack()
            },
            onSelectBookshelf = { dialogSelectedBookshelves.add(it) },
            onDeselectBookshelf = dialogSelectedBookshelves::remove,
            allBookshelf = allBookshelves,
            selectedBookshelfIds = dialogSelectedBookshelves
        )
    }
}

private fun Navigator.navigateToAddBookToBookshelfDialog(selectedBookIds: List<String>) {
    navigate(Route.Main.Bookshelf.AddBookToBookshelfDialog(selectedBookIds))
}
