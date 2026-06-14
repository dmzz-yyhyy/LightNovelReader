package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.ui.components.DeleteBookshelfDialog
import io.nightfish.lightnovelreader.api.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.bookshelfEditDestination() {
    composable<Route.Main.Bookshelf.Edit> {
        val navController = LocalNavController.current
        val editBookshelfViewModel = hiltViewModel<EditBookshelfViewModel>()
        val edit = it.toRoute<Route.Main.Bookshelf.Edit>()
        EditBookshelfScreen(
            title = edit.title,
            bookshelfId = edit.id,
            bookshelf = editBookshelfViewModel.uiState,
            init = editBookshelfViewModel::init,
            onClickBack = navController::popBackStackIfResumed,
            onClickSave = {
                navController.popBackStackIfResumed()
                editBookshelfViewModel.save()
            },
            onClickDelete = navController::navigateToDeleteBookshelfDialog,
            onNameChange = editBookshelfViewModel::onNameChange,
            onSortTypeChange = editBookshelfViewModel::onSortTypeChange,
            onAutoCacheChange = editBookshelfViewModel::onAutoCacheChange,
            onSystemUpdateReminderChange = editBookshelfViewModel::onSystemUpdateReminderChange,
        )
    }
    deleteBookshelfDialog()
}

fun NavController.navigateToBookshelfEditDestination(id: Int, title: String) {
    navigate(Route.Main.Bookshelf.Edit(id, title))
}

private fun NavGraphBuilder.deleteBookshelfDialog() {
    dialog<Route.Main.Bookshelf.DeleteBookshelfDialog> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<DeleteBookshelfDialogViewModel>()
        DeleteBookshelfDialog(
            onDismissRequest = { navController.popBackStack() },
            onConfirmation = {
                viewModel.deleteBookshelf(it.toRoute<Route.Main.Bookshelf.DeleteBookshelfDialog>().bookshelfId)
                navController.popBackStack()
                navController.popBackStackIfResumed()
            }
        )
    }
}

private fun NavController.navigateToDeleteBookshelfDialog(bookId: Int) {
    if (!this.isResumed()) return
    navigate(Route.Main.Bookshelf.DeleteBookshelfDialog(bookId))
}
