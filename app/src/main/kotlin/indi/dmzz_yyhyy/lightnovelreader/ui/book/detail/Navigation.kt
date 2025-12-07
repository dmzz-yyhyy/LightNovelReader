package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.work.WorkInfo
import indi.dmzz_yyhyy.lightnovelreader.R
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToBookReaderDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToImageViewerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToAddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToMarkAllChaptersAsReadDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun NavGraphBuilder.bookDetailDestination() {
    composable<Route.Book.Detail> { entry ->
        val navController = LocalNavController.current
        val bookId = entry.toRoute<Route.Book.Detail>().bookId
        val viewModel = hiltViewModel<DetailViewModel>(entry)
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val exportBookToEPUBLauncher = uriLauncher { uri ->
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, context.getString(R.string.export_book_started, viewModel.uiState.bookInformation.title), Toast.LENGTH_SHORT).show()
                viewModel.exportToEpub(uri, bookId, viewModel.uiState.bookInformation.title).collect {
                    if (it != null)
                        when (it.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                Toast.makeText(context, context.getString(R.string.export_book_success, viewModel.uiState.bookInformation.title), Toast.LENGTH_SHORT).show()
                            }
                            WorkInfo.State.FAILED -> {
                                Toast.makeText(context, context.getString(R.string.export_book_failed, viewModel.uiState.bookInformation.title), Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                }
            }
            navController.popBackStack()
        }
        viewModel.navController = navController
        val snackbarHostState = LocalSnackbarHost.current

        LaunchedEffect(bookId) {
            viewModel.init(bookId)
        }
        DetailScreen(
            uiState = viewModel.uiState,
            onClickExportToEpub = { settings ->
                viewModel.exportSettings = settings
                when (settings.exportType) {
                    ExportType.BOOK -> createDataFile(context, viewModel.uiState.bookInformation.title, exportBookToEPUBLauncher)
                    ExportType.VOLUMES -> selectDirectory(context, exportBookToEPUBLauncher)
                }
            },
            onClickBackButton = navController::popBackStackIfResumed,
            onClickChapter = {
                navController.navigateToBookReaderDestination(bookId, it, context)
            },
            onClickReadFromStart = {
                viewModel.uiState.bookVolumes.volumes.firstOrNull()?.chapters?.firstOrNull()?.id?.let {
                    navController.navigateToBookReaderDestination(bookId, it, context)
                }
            },
            onClickContinueReading = {
                if (viewModel.uiState.userReadingData.lastReadChapterId.isBlank())
                    viewModel.uiState.bookVolumes.volumes.firstOrNull()?.chapters?.firstOrNull()?.id?.let {
                        navController.navigateToBookReaderDestination(bookId, it, context)
                    }
                else {
                    navController.navigateToBookReaderDestination(bookId, viewModel.uiState.userReadingData.lastReadChapterId, context)
                }
            },
            cacheBook = { bookId ->
                coroutineScope.launch {
                    viewModel.cacheBook(bookId).collect {
                        if (it == null) {
                            showSnackbar(
                                coroutineScope = coroutineScope,
                                hostState = snackbarHostState,
                                message = context.getString(
                                    R.string.cache_book_started,
                                    viewModel.uiState.bookInformation.title
                                )
                            ) { }
                            return@collect
                        }
                        when (it.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                showSnackbar(
                                    coroutineScope = coroutineScope,
                                    hostState = snackbarHostState,
                                    message = context.getString(R.string.cache_book_finished)
                                ) { }
                            }
                            WorkInfo.State.FAILED -> {
                                showSnackbar(
                                    coroutineScope = coroutineScope,
                                    hostState = snackbarHostState,
                                    message = context.getString(R.string.cache_book_error)
                                ) { }
                            }
                            WorkInfo.State.RUNNING -> {
                                showSnackbar(
                                    coroutineScope = coroutineScope,
                                    hostState = snackbarHostState,
                                    message = context.getString(R.string.cache_book_running)
                                ) { }
                            }
                            else -> {}
                        }
                    }
                }
            },
            requestAddBookToBookshelf = navController::navigateToAddBookToBookshelfDialog,
            onClickTag = viewModel::onClickTag,
            onClickCover = navController::navigateToImageViewerDialog,
            onClickMarkAsRead = { navController.navigateToMarkAllChaptersAsReadDialog(viewModel.uiState.bookInformation.id) }
        )
    }
}

fun NavController.navigateToBookDetailDestination(bookId: String) {
    if (!this.isResumed()) return
    navigate(Route.Book.Detail(bookId))
}

@Suppress("DuplicatedCode")
fun createDataFile(
    context: Context,
    fileName: String,
    launcher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Documents")
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/epub+zip"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
        putExtra(Intent.EXTRA_TITLE, fileName)
    }
    launcher.launch(Intent.createChooser(intent, context.getString(R.string.select_location)))
}

@Suppress("DuplicatedCode")
fun selectDirectory(context: Context, launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, Intent.ACTION_OPEN_DOCUMENT)
    }
    launcher.launch(Intent.createChooser(intent, context.getString(R.string.select_location)))
}

