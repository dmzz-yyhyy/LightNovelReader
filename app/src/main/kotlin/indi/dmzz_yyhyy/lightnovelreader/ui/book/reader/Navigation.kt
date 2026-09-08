package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.overlay.overlayEntry
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.imageview.ImageViewerScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.imageview.ImageViewerViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ColorPickerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.readerstyle.navigateToSettingsReaderStyleDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.ImageUtils.saveBitmapAsPng
import indi.dmzz_yyhyy.lightnovelreader.utils.ImageUtils.uriToBitmap
import io.nightfish.lightnovelreader.api.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun NavEntryScope.bookReaderDestination() {
    entry<Route.Book.Reader> { route ->
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<ReaderViewModel>()
        LaunchedEffect(route) {
            viewModel.bookId = route.bookId
            viewModel.changeChapter(route.chapterId)
        }
        ReaderScreen(
            readingScreenUiState = viewModel.uiState,
            settingState = viewModel.settingState,
            onClickBackButton = { navigator.popBackStack() },
            updateTotalReadingTime = viewModel::updateTotalReadingTime,
            accumulateReadTime = viewModel::accumulateReadingTime,
            onClickPrevChapter = viewModel::prevChapter,
            onClickNextChapter = viewModel::nextChapter,
            onChangeChapter = viewModel::changeChapter,
            onClickReaderStyleSettings = navigator::navigateToSettingsReaderStyleDestination
        )
    }
    colorPickerDialog()
    imageViewerDialog()
}

fun Navigator.navigateToBookReaderDestination(
    bookId: String,
    chapterId: String,
) {
    navigate(Route.Book.Reader(bookId, chapterId))
}

private fun NavEntryScope.colorPickerDialog() {
    overlayEntry<Route.Book.ColorPickerDialog> { entry ->
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<ColorPickerDialogViewModel>()
        val selectedColor by viewModel.init(entry.colorUserDataPath)
            .collectAsStateWithLifecycle(Color.Unspecified)
        ColorPickerDialog(
            onDismissRequest = { navigator.popBackStack() },
            onConfirmation = {
                viewModel.changeBackgroundColor(it)
                navigator.popBackStack()
            },
            selectedColor = selectedColor ?: Color.Unspecified,
            colors = entry.colors.map { Color(if (it < 0) return@map Color.Unspecified else it) },
            description = stringResource(entry.target.toAppTarget().descriptionResId)
        )
    }
}

fun Navigator.navigateToColorPickerDialog(
    colorUserDataPath: String,
    colors: List<Long>,
    target: Route.Book.ColorPickerTargetType = Route.Book.ColorPickerTargetType.BACKGROUND
) {
    navigate(Route.Book.ColorPickerDialog(colorUserDataPath, colors.toLongArray(), target))
}

@SuppressLint("LocalContextGetResourceValueCall")
private fun NavEntryScope.imageViewerDialog() {
    overlayEntry<Route.Book.ImageViewerDialog> { entry ->
        val navigator = LocalNavigator.current
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
                        imageUri = entry.imageUri.toUri(),
                        context = context,
                        header = viewModel.imageHeader
                    ).onOk { bitmap ->
                        val result = runCatching {
                            context.contentResolver.openOutputStream(targetUri)?.use { out ->
                                bitmap.compress(
                                    Bitmap.CompressFormat.PNG,
                                    100,
                                    out
                                )
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
                    }.onErr {
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
            imageUri = entry.imageUri.toUri(),
            onDismissRequest = { navigator.popBackStack() },
            onClickSave = {
                coroutineScope.launch(Dispatchers.IO) {
                    uriToBitmap(
                        imageUri = entry.imageUri.toUri(),
                        context = context,
                        header = viewModel.imageHeader
                    ).onOk {
                        coroutineScope.launch {
                            saveBitmapAsPng(context, it)
                                .onOk { path ->
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.saved_to_pictures_dir, path),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                .onErr {
                                    Toast.makeText(
                                        context,
                                        saveFailed,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }.onErr {
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

fun Navigator.navigateToImageViewerDialog(
    imageUri: Uri
) {
    navigate(
        Route.Book.ImageViewerDialog(
            imageUri = imageUri.toString()
        )
    )
}
