package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
fun NavGraphBuilder.bookshelfHomeDestination(sharedTransitionScope: SharedTransitionScope) {
    composable<Route.Main.Bookshelf.Home> {
        LocalContext.current
        val navController = LocalNavController.current
        val bookshelfHomeViewModel = hiltViewModel<BookshelfHomeViewModel>()
        val bookshelfNewTitle = stringResource(R.string.bookshelf_new_title)
        val bookshelfEditTitle = stringResource(R.string.bookshelf_edit_title)
        BookshelfHomeScreen(
            init = bookshelfHomeViewModel::load,
            changePage = bookshelfHomeViewModel::changePage,
            changeBookSelectState = bookshelfHomeViewModel::changeBookSelectState,
            uiState = bookshelfHomeViewModel.uiState,
            onClickCreate = {
                navController.navigateToBookshelfEditDestination(-1, bookshelfNewTitle)
            },
            onClickEdit = {
                navController.navigateToBookshelfEditDestination(it, bookshelfEditTitle)
            },
            onClickBook = navController::navigateToBookDetailDestination,
            onClickEnableSelectMode = bookshelfHomeViewModel::enableSelectMode,
            onClickDisableSelectMode = bookshelfHomeViewModel::disableSelectMode,
            onClickSelectAll = bookshelfHomeViewModel::selectAllBooks,
            onClickPin = bookshelfHomeViewModel::pinSelectedBooks,
            onClickRemove = {
                bookshelfHomeViewModel.removeSelectedBooks()
                if (bookshelfHomeViewModel.uiState.selectedBookshelf.allBookIds.isEmpty())
                    bookshelfHomeViewModel.disableSelectMode()
            },
            saveAllBookshelfJsonData = bookshelfHomeViewModel::saveAllBookshelf,
            saveBookshelfJsonData = bookshelfHomeViewModel::saveThisBookshelf,
            importBookshelf = bookshelfHomeViewModel::importBookshelf,
            onClickMarkSelectedBooks = {
                navController.navigateToAddBookToBookshelfDialog(bookshelfHomeViewModel.uiState.selectedBookIds)
                bookshelfHomeViewModel.disableSelectMode()
            },
            clearToast = bookshelfHomeViewModel::clearToast,
            sharedTransitionScope = sharedTransitionScope,
            getBookInfoFlow = bookshelfHomeViewModel::getBookInfoStateFlow,
            getBookVolumesFlow = bookshelfHomeViewModel::getBookVolumesStateFlow
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