package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.detail.navigateToBookDetailDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.AddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit.navigateToBookshelfEditDestination
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
fun NavGraphBuilder.bookshelfHomeDestination(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Bookshelf.Home> {
        val navController = LocalNavController.current
        val parentEntry = remember(it) { navController.getBackStackEntry(Route.Main) }
        val bookshelfHomeViewModel = hiltViewModel<BookshelfHomeViewModel>(parentEntry)
        val bookshelfNewTitle = stringResource(R.string.bookshelf_new_title)
        val bookshelfEditTitle = stringResource(R.string.bookshelf_edit_title)
        val uiState = remember(navController, bookshelfHomeViewModel, bookshelfNewTitle, bookshelfEditTitle) {
            object : BookshelfHomeUiState by bookshelfHomeViewModel.uiState {
                override val enableReorderMode: () -> Unit = {
                    navController.navigate(Route.Main.Bookshelf.ReorderBooks(bookshelfHomeViewModel.uiState.selectedBookshelfId))
                }
                override val enableBookshelfReorderMode: () -> Unit = {
                    navController.navigate(Route.Main.Bookshelf.ReorderBookshelves)
                }
                override val onCreate: () -> Unit = {
                    navController.navigateToBookshelfEditDestination(-1, bookshelfNewTitle)
                }
                override val onEdit: (Int) -> Unit = { bookshelfId ->
                    navController.navigateToBookshelfEditDestination(bookshelfId, bookshelfEditTitle)
                }
                override val onBookClick: (String) -> Unit = navController::navigateToBookDetailDestination
                override val onRemove: () -> Unit = {
                    bookshelfHomeViewModel.removeSelectedBooks()
                    if (bookshelfHomeViewModel.uiState.selectedBookshelf.allBookIds.isEmpty()) {
                        bookshelfHomeViewModel.disableSelectMode()
                    }
                }
                override val onMarkSelectedBooks: () -> Unit = {
                    navController.navigateToAddBookToBookshelfDialog(bookshelfHomeViewModel.uiState.selectedBookIds)
                    bookshelfHomeViewModel.disableSelectMode()
                }
            }
        }
        BookshelfHomeScreen(
            init = bookshelfHomeViewModel::load,
            uiState = uiState,
            dataSources = BookshelfHomeDataSources(
                getBookInfoFlow = bookshelfHomeViewModel::getBookInfoStateFlow,
                getBookVolumesFlow = bookshelfHomeViewModel::getBookVolumesStateFlow,
                getBookMetadataFlow = bookshelfHomeViewModel::getBookshelfBookMetadataStateFlow
            )
        )
    }

    addBookToBookshelfDialog()
}

@Suppress("unused")
fun NavController.navigateToBookshelfHomeDestination() {
    navigate(Route.Main.Bookshelf.Home)
}

private fun NavGraphBuilder.addBookToBookshelfDialog() {
    dialog<Route.Main.Bookshelf.AddBookToBookshelfDialog> { entry ->
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<AddBookToBookshelfDialogViewModel>()
        val dialogSelectedBookshelves = remember { mutableStateListOf<Int>() }
        val route = entry.toRoute<Route.Main.Bookshelf.AddBookToBookshelfDialog>()
        val allBookshelves by viewModel.allBookshelfFlow.collectAsState(emptyList<Bookshelf>())
        AddBookToBookshelfDialog(
            onDismissRequest = { navController.popBackStack() },
            onConfirmation = {
                CoroutineScope(Dispatchers.Main).launch {
                    viewModel.markSelectedBooks(route.selectedBookIds, dialogSelectedBookshelves)
                }
                navController.popBackStack()
            },
            onSelectBookshelf = { dialogSelectedBookshelves.add(it) },
            onDeselectBookshelf = dialogSelectedBookshelves::remove,
            allBookshelf = allBookshelves,
            selectedBookshelfIds = dialogSelectedBookshelves
        )
    }
}

private fun NavController.navigateToAddBookToBookshelfDialog(selectedBookIds: List<String>) {
    navigate(Route.Main.Bookshelf.AddBookToBookshelfDialog(selectedBookIds))
}
