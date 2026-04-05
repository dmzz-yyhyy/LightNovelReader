package indi.dmzz_yyhyy.lightnovelreader.utils

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Scale
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.SettingState
import io.nightfish.lightnovelreader.api.userdata.UriUserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

private const val KRAFT_PAPER_URL = "https://portal.curiousers.org/static/lnr/paper.webp"
private const val KRAFT_PAPER_CACHE_KEY = "default_kraft_paper"

fun loadReaderFontFamilySafe(uri: Uri): FontFamily? {
    return try {
        if (uri == Uri.EMPTY) return null
        val fontFile = File(uri.path ?: return null)
        if (!fontFile.exists()) throw FileNotFoundException()
        FontFamily(Font(fontFile))
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
    if (fontFamily == null && uri != Uri.EMPTY) {
        LaunchedEffect(uri) {
            withContext(Dispatchers.IO) { fontFamilyUriUserData.set(Uri.EMPTY) }
            snackbarScope.launch {
                snackbarHostState.showSnackbar("自定义字体加载失败，已恢复为默认。")
            }
        }
    }

    return fontFamily ?: FontFamily.Default
}

@Composable
private fun rememberPaperPainter(
    snackbarScope: CoroutineScope,
): Painter {
    val context = LocalContext.current
    val theme = LocalAppTheme.current
    val fallback = remember(theme.isDark, theme.colorScheme.background) {
        ColorPainter(theme.colorScheme.background)
    }
    val snackbarHostState = LocalSnackbarHost.current

    val request = remember {
        ImageRequest.Builder(context)
            .data(KRAFT_PAPER_URL)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .memoryCacheKey(KRAFT_PAPER_CACHE_KEY)
            .diskCacheKey(KRAFT_PAPER_CACHE_KEY)
            .interceptorCoroutineContext(Dispatchers.Default)
            .scale(Scale.FILL)
            .build()
    }

    val painter = rememberAsyncImagePainter(
        model = request,
        placeholder = fallback,
        error = fallback
    )

    var errorNotified by remember { mutableStateOf(false) }

    LaunchedEffect(painter) {
        painter.state.collect { state ->
            when (state) {
                is AsyncImagePainter.State.Error -> {
                    if (!errorNotified) {
                        errorNotified = true
                        snackbarScope.launch {
                            snackbarHostState.showSnackbar("自定义背景加载失败，已恢复为默认。")
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

    if (backgroundUri == Uri.EMPTY || backgroundUri.toString().isBlank()) {
        return rememberPaperPainter(snackbarScope)
    }

    return rememberAsyncImagePainter(
        model = backgroundUri
    )
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
