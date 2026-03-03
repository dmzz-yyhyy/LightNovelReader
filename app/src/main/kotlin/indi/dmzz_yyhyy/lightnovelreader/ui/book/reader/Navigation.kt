package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.HiltViewModelFactory
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.imageview.ImageViewerScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.imageview.ImageViewerViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ColorPickerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme.navigateToSettingsThemeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
import indi.dmzz_yyhyy.lightnovelreader.utils.ImageUtils.saveBitmapAsPng
import indi.dmzz_yyhyy.lightnovelreader.utils.ImageUtils.uriToBitmap
import indi.dmzz_yyhyy.lightnovelreader.utils.isResumed
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun NavGraphBuilder.bookReaderDestination() {
    composable<Route.Book.Reader> { navBackStackEntry ->
        val navController = LocalNavController.current
        val parentEntry = remember(navBackStackEntry) {
            navBackStackEntry.destination.parent?.route
                ?.let(navController::getBackStackEntry)
        }
        val viewModel = hiltViewModel<ReaderViewModel>(parentEntry ?: navBackStackEntry)
        ReaderScreen(
            readingScreenUiState = viewModel.uiState,
            settingState = viewModel.settingState,
            onClickBackButton = navController::popBackStackIfResumed,
            updateTotalReadingTime = viewModel::updateTotalReadingTime,
            accumulateReadTime = viewModel::accumulateReadingTime,
            onClickPrevChapter = viewModel::prevChapter,
            onClickNextChapter = viewModel::nextChapter,
            onChangeChapter = viewModel::changeChapter,
            onClickThemeSettings = navController::navigateToSettingsThemeDestination
        )
    }
    colorPickerDialog()
    imageViewerDialog()
}

fun NavController.navigateToBookReaderDestination(bookId: String, chapterId: String, context: Context) {
    val entry = this.getBackStackEntry<Route.Book>()
    val viewModel = ViewModelProvider.create(
        entry,
        HiltViewModelFactory(
            context = context,
            delegateFactory = entry.defaultViewModelProviderFactory
        ),
    )[ReaderViewModel::class.java]
    viewModel.bookId = bookId
    viewModel.changeChapter(chapterId)
    this.navigate(Route.Book.Reader)
}

private fun NavGraphBuilder.colorPickerDialog() {
    dialog<Route.Book.ColorPickerDialog> { entry ->
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<ColorPickerDialogViewModel>()
        val route = entry.toRoute<Route.Book.ColorPickerDialog>()
        val selectedColor by viewModel.init(route.colorUserDataPath).collectAsState(Color.Unspecified)
        ColorPickerDialog(
            onDismissRequest = { navController.popBackStack() },
            onConfirmation = {
                viewModel.changeBackgroundColor(it)
                navController.popBackStack()
            },
            selectedColor = selectedColor ?: Color.Unspecified,
            colors = route.colors.map { Color(if (it < 0) return@map Color.Unspecified else it) }
        )
    }
}

fun NavController.navigateToColorPickerDialog(colorUserDataPath: String, colors: List<Long>) {
    if (!this.isResumed()) return
    navigate(Route.Book.ColorPickerDialog(colorUserDataPath, colors.toLongArray()))
}
@SuppressLint("LocalContextGetResourceValueCall")
private fun NavGraphBuilder.imageViewerDialog() {
    dialog<Route.Book.ImageViewerDialog>(
        dialogProperties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) { entry ->
        val navController = LocalNavController.current
        val route = entry.toRoute<Route.Book.ImageViewerDialog>()
        val viewModel = hiltViewModel<ImageViewerViewModel>()

        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        val savedToPicturesDir = stringResource(R.string.saved_to_pictures_dir, "")
        val saveFailed = stringResource(R.string.save_failed, "")

        val createDocumentLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("image/png")
            ) { targetUri ->
                if (targetUri == null) return@rememberLauncherForActivityResult

                coroutineScope.launch(Dispatchers.IO) {
                    uriToBitmap(
                        imageUri = route.imageUri.toUri(),
                        context = context,
                        header = viewModel.imageHeader
                    ).onSuccess { bitmap ->
                        val result = runCatching {
                            context.contentResolver.openOutputStream(targetUri)?.use { out ->
                                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                            } ?: error("Cannot open output stream")
                        }
                        result.onSuccess {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    savedToPicturesDir,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }.onFailure {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    saveFailed,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }.onFailure {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                saveFailed,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

        ImageViewerScreen(
            imageUri = route.imageUri.toUri(),
            onDismissRequest = { navController.popBackStack() },
            onClickSave = {
                coroutineScope.launch(Dispatchers.IO) {
                    uriToBitmap(
                        imageUri = route.imageUri.toUri(),
                        context = context,
                        header = viewModel.imageHeader
                    )
                        .onSuccess {
                            coroutineScope.launch {
                                saveBitmapAsPng(context, it)
                                    .onSuccess { path ->
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.saved_to_pictures_dir, path),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    .onFailure {
                                        Toast.makeText(
                                            context,
                                            saveFailed,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        }
                        .onFailure {
                            Log.d("ImageViewer", "Failed to save image: ${it.message}")
                            Toast.makeText(
                                context,
                                context.getString(R.string.save_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            },
            onLongClickSave = {
                val defaultName = "lnr_${System.currentTimeMillis()}.png"
                createDocumentLauncher.launch(defaultName)
            },
            header = viewModel.imageHeader
        )
    }
}

fun NavController.navigateToImageViewerDialog(
    imageUri: Uri
) {
    navigate(
        Route.Book.ImageViewerDialog(
            imageUri = imageUri.toString()
        )
    )
}