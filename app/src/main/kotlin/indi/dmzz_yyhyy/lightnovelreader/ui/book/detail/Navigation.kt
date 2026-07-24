package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import android.annotation.SuppressLint
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
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToBookReaderDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToImageViewerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToAddBookToBookshelfDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.dialog.navigateToMarkAllChaptersAsReadDialog
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import indi.dmzz_yyhyy.lightnovelreader.utils.uriLauncher
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("LocalContextGetResourceValueCall")
fun NavGraphBuilder.bookDetailDestination() {
    composable<Route.Book.Detail> { entry ->
        val navController = LocalNavController.current
        val bookId = entry.toRoute<Route.Book.Detail>().bookId
        val viewModel = hiltViewModel<DetailViewModel>(entry)
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val exportBookToEPUBLauncher = uriLauncher { uri ->
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.uiState.bookInformation
                    ?.map { it.title }
                    ?.onOk { title ->
                        Toast.makeText(context, context.getString(R.string.export_book_started, title), Toast.LENGTH_SHORT).show()
                        viewModel.exportToEpub(uri, bookId, title).collect {
                            if (it != null)
                                when (it.state) {
                                    WorkInfo.State.SUCCEEDED -> {
                                        Toast.makeText(context, context.getString(R.string.export_book_success, it), Toast.LENGTH_SHORT).show()
                                    }
                                    WorkInfo.State.FAILED -> {
                                        Toast.makeText(context, context.getString(R.string.export_book_failed, it), Toast.LENGTH_SHORT).show()
                                    }
                                    else -> {}
                                }
                        }
                    }?.onErr {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
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

                viewModel.uiState.bookInformation
                    ?.map { it.title }
                    ?.onOk { title ->
                        when (settings.exportType) {
                            ExportType.BOOK -> createDataFile(context, title, exportBookToEPUBLauncher)
                            ExportType.VOLUMES -> selectDirectory(context, exportBookToEPUBLauncher)
                        }
                    }?.onErr {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
            },
            onClickBackButton = navController::popBackStackIfResumed,
            onClickChapter = {
                navController.navigateToBookReaderDestination(bookId, it, context)
            },
            onClickReadFromStart = {
                viewModel.uiState.bookVolumes
                    ?.map {
                        it.volumes.firstOrNull()?.chapters?.firstOrNull()?.id
                    }?.onOk { id ->
                        id?.let {
                            navController.navigateToBookReaderDestination(bookId, it, context)
                        }
                    }?.onErr {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
            },
            onClickContinueReading = {
                if (viewModel.uiState.userReadingData?.lastReadChapterId == null)
                    viewModel.uiState.bookVolumes
                        ?.map {
                            it.volumes.firstOrNull()?.chapters?.firstOrNull()?.id
                        }?.onOk { id ->
                            id?.let {
                                navController.navigateToBookReaderDestination(bookId, it, context)
                            }
                        }?.onErr {
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        }
                else {
                    navController.navigateToBookReaderDestination(bookId, viewModel.uiState.userReadingData!!.lastReadChapterId!!, context)
                }
            },
            cacheBook = { bookId ->
                coroutineScope.launch {
                    viewModel.cacheBook(bookId).collect { workInfo ->
                        if (workInfo == null) {
                            viewModel.uiState.bookInformation
                                ?.map { it.title }
                                ?.onOk { title ->
                                    showSnackbar(
                                        coroutineScope = coroutineScope,
                                        hostState = snackbarHostState,
                                        message = context.getString(
                                            R.string.cache_book_started,
                                            title
                                        )
                                    ) { }
                                }?.onErr {
                                    showSnackbar(
                                        coroutineScope = coroutineScope,
                                        hostState = snackbarHostState,
                                        message = it.message
                                    ) { }
                                }
                            return@collect
                        }
                        when (workInfo.state) {
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
            onClickMarkAsRead = {
                navController.navigateToMarkAllChaptersAsReadDialog(bookId)
            }
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

