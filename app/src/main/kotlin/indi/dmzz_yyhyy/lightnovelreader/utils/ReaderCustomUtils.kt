package indi.dmzz_yyhyy.lightnovelreader.utils

import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import io.nightfish.lightnovelreader.api.userdata.UriUserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

fun loadReaderFontFamilySafe(uri: Uri): FontFamily? {
    return try {
        if (uri == Uri.EMPTY || uri.toString().isBlank()) return null
        val fontFile = File(uri.path ?: return null)
        if (!fontFile.exists()) throw FileNotFoundException()
        val typeface = Typeface.createFromFile(fontFile) ?: throw IllegalArgumentException("Invalid font file")
        FontFamily(typeface)
    } catch (e: Exception) {
        Log.e("FontLoad", "Failed to load custom font", e)
        null
    }
}

@Composable
fun rememberReaderFontFamily(
    fontFamilyUriUserData: UriUserData,
): FontFamily {
    val snackbarScope = rememberCoroutineScope()
    val uri by fontFamilyUriUserData.getFlowWithDefault(Uri.EMPTY).collectAsState(Uri.EMPTY)
    val fontFamily = remember(uri) { loadReaderFontFamilySafe(uri) }

    val snackbarHostState = LocalSnackbarHost.current
    val message = stringResource(R.string.reader_custom_font_load_failed)
    if (fontFamily == null && uri != Uri.EMPTY && uri.toString().isNotBlank()) {
        LaunchedEffect(uri) {
            withContext(Dispatchers.IO) { fontFamilyUriUserData.set(Uri.EMPTY) }
            snackbarScope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    return fontFamily ?: FontFamily.Default
}

@Composable
private fun rememberCustomBackgroundPainter(
    uri: Uri,
    backgroundUriUserData: UriUserData,
    fallback: Painter,
    snackbarScope: CoroutineScope,
): Painter {
    val snackbarHostState = LocalSnackbarHost.current
    val message = stringResource(R.string.reader_custom_background_load_failed)

    val painter = rememberAsyncImagePainter(
        model = uri,
        error = fallback
    )

    var errorNotified by remember(uri) { mutableStateOf(false) }

    LaunchedEffect(painter) {
        painter.state.collect { state ->
            when (state) {
                is AsyncImagePainter.State.Error -> {
                    Log.e(
                        "CustomBackground",
                        "Failed to load background image: $uri",
                        state.result.throwable
                    )

                    if (!errorNotified) {
                        errorNotified = true
                        withContext(Dispatchers.IO) {
                            backgroundUriUserData.set(Uri.EMPTY)
                        }
                        snackbarScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                }
                is AsyncImagePainter.State.Success -> {
                    errorNotified = false
                }
                else -> Unit
            }
        }
    }

    return painter
}

@Composable
fun rememberReaderBackgroundPainter(
    settingState: SettingState,
): Painter {
    val isDark = LocalAppTheme.current.isDark
    val snackbarScope = rememberCoroutineScope()

    val backgroundUri = remember(
        isDark,
        settingState.backgroundImageUri,
        settingState.backgroundDarkImageUri
    ) {
        if (isDark) settingState.backgroundDarkImageUri else settingState.backgroundImageUri
    }

    val fallback = remember { ColorPainter(Color.Transparent) }

    if (backgroundUri == Uri.EMPTY || backgroundUri.toString().isBlank()) {
        return fallback
    }

    val backgroundUriUserData = if (isDark) {
        settingState.backgroundDarkImageUriUserData
    } else {
        settingState.backgroundImageUriUserData
    }

    return rememberCustomBackgroundPainter(backgroundUri, backgroundUriUserData, fallback, snackbarScope)
}

@Composable
fun readerBackgroundColor(settingState: SettingState): Color {
    val localTheme = LocalAppTheme.current
    val isDark = localTheme.isDark
    val background = localTheme.colorScheme.background

    val color = remember(isDark, settingState.backgroundColor, settingState.backgroundDarkColor, background) {
        when {
            isDark && settingState.backgroundDarkColor.isUnspecified -> background
            !isDark && settingState.backgroundColor.isUnspecified -> background
            isDark -> settingState.backgroundDarkColor
            else -> settingState.backgroundColor
        }
    }

    return color
}

@Composable
fun readerTextColor(settingState: SettingState): Color {
    val localTheme = LocalAppTheme.current
    val isDark = localTheme.isDark
    val onSurface = localTheme.colorScheme.onSurface

    val color = remember(isDark, settingState.textColor, settingState.textDarkColor, onSurface) {
        when {
            isDark && settingState.textDarkColor.isUnspecified -> onSurface
            !isDark && settingState.textColor.isUnspecified -> onSurface
            isDark -> settingState.textDarkColor
            else -> settingState.textColor
        }
    }

    return color
}
