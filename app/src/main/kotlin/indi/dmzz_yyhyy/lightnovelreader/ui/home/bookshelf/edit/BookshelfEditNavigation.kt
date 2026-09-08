package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.edit

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.overlay.overlayEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.DeleteBookshelfDialog
import io.nightfish.lightnovelreader.api.Route

fun NavEntryScope.bookshelfEditDestination() {
    entry<Route.Main.Bookshelf.Edit> {
        val navigator = LocalNavigator.current
        val editBookshelfViewModel = hiltViewModel<EditBookshelfViewModel>()
        EditBookshelfScreen(
            title = it.title,
            bookshelfId = it.id,
            bookshelf = editBookshelfViewModel.bookshelf,
            init = editBookshelfViewModel::init,
            onClickBack = navigator::popBackStack,
            onClickSave = {
                navigator.popBackStack()
                editBookshelfViewModel.save()
            },
            onClickDelete = navigator::navigateToDeleteBookshelfDialog,
            onNameChange = editBookshelfViewModel::onNameChange,
            onSortTypeChange = editBookshelfViewModel::onSortTypeChange,
            onAutoCacheChange = editBookshelfViewModel::onAutoCacheChange,
            onSystemUpdateReminderChange = editBookshelfViewModel::onSystemUpdateReminderChange,
        )
    }
    deleteBookshelfDialog()
}

fun Navigator.navigateToBookshelfEditDestination(id: Int, title: String) {
    navigate(Route.Main.Bookshelf.Edit(id, title))
}

private fun NavEntryScope.deleteBookshelfDialog() {
    overlayEntry<Route.Main.Bookshelf.DeleteBookshelfDialog> {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<DeleteBookshelfDialogViewModel>()
        DeleteBookshelfDialog(
            onDismissRequest = { navigator.popBackStack() },
            onConfirmation = {
                viewModel.deleteBookshelf(it.bookshelfId)
                navigator.popBackStack()
                navigator.popBackStack()
            }
        )
    }
}

private fun Navigator.navigateToDeleteBookshelfDialog(bookId: Int) {
    navigate(Route.Main.Bookshelf.DeleteBookshelfDialog(bookId))
}
