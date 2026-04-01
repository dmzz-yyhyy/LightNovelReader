package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.BaseDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.components.CheckBoxListItem
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import io.nightfish.lightnovelreader.api.ui.LocalNavController

fun NavGraphBuilder.addBookToBookshelfDialog() {
    dialog<Route.AddBookToBookshelfDialog> {
        val navController = LocalNavController.current
        val addToBookshelfDialogViewModel = hiltViewModel<AddToBookshelfDialogViewModel>()
        val route = it.toRoute<Route.AddBookToBookshelfDialog>()
        addToBookshelfDialogViewModel.bookId = route.bookId
        addToBookshelfDialogViewModel.navController = navController
        AddBookToBookshelfDialog(
            onDismissRequest = addToBookshelfDialogViewModel::onDismissAddToBookshelfRequest,
            onConfirmation = addToBookshelfDialogViewModel::processAddToBookshelfRequest,
            onSelectBookshelf = addToBookshelfDialogViewModel::onSelectBookshelf,
            onDeselectBookshelf = addToBookshelfDialogViewModel::onDeselectBookshelf,
            allBookshelf = addToBookshelfDialogViewModel.addToBookshelfDialogUiState.allBookShelf,
            selectedBookshelfIds = addToBookshelfDialogViewModel.addToBookshelfDialogUiState.selectedBookshelfIds
        )
    }
}

fun NavController.navigateToAddBookToBookshelfDialog(bookId: String) {
    navigate(Route.AddBookToBookshelfDialog(bookId))
}

@Composable
fun AddBookToBookshelfDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    onSelectBookshelf: (Int) -> Unit,
    onDeselectBookshelf: (Int) -> Unit,
    allBookshelf: List<Bookshelf>,
    selectedBookshelfIds: List<Int>
) {
    val scrollState = rememberScrollState()
    BaseDialog(
        icon = painterResource(R.drawable.filled_bookmark_24px),
        title = stringResource(R.string.add_to_bookshelf),
        description = stringResource(R.string.dialog_add_to_bookshelf_text),
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        dismissText = stringResource(R.string.cancel),
        confirmationText = stringResource(R.string.add_to_bookshelf),
    ) {
        Column(Modifier.width(IntrinsicSize.Max).sizeIn(maxHeight = 350.dp).verticalScroll(scrollState)) {
            if (allBookshelf.isEmpty()) {
                CheckBoxListItem(
                    modifier = Modifier
                        .wrapContentWidth()
                        .sizeIn(minWidth = 325.dp)
                        .padding(horizontal = 14.dp),
                    title = "",
                    supportingText = "",
                    checked = false,
                    onCheckedChange = { }
                )
            }
            allBookshelf.forEachIndexed { index, bookshelf ->
                CheckBoxListItem(
                    modifier = Modifier
                        .wrapContentWidth()
                        .sizeIn(minWidth = 325.dp)
                        .padding(horizontal = 14.dp),
                    title = bookshelf.name,
                    supportingText = stringResource(R.string.bookshelf_book_count, bookshelf.allBookIds.size),
                    checked = selectedBookshelfIds.contains(bookshelf.id),
                    onCheckedChange = {
                        if (it) onSelectBookshelf(bookshelf.id) else onDeselectBookshelf(
                            bookshelf.id
                        )
                    }
                )
                if (index != allBookshelf.size - 1) {
                    HorizontalDivider(Modifier.padding(horizontal = 14.dp))
                }
            }
        }
    }
}
