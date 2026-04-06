package indi.dmzz_yyhyy.lightnovelreader.theme

import android.app.Activity
import android.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalDarkColorScheme
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalLightColorScheme
import io.nightfish.lightnovelreader.api.ui.LocalTextLocaleList
import io.nightfish.lightnovelreader.api.ui.appLocaleToTextLocaleList
import io.nightfish.lightnovelreader.api.ui.theme.AppTypography
import androidx.compose.ui.graphics.Color as ComposeColor

data class AppTheme(
    val isDark: Boolean,
    val colorScheme: ColorScheme
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LightNovelReaderTheme(
    darkMode: String,
    isDynamicColor: Boolean = true,
    dynamicBlack: Boolean = false,
    enableCostumeThemeScheme: Boolean = false,
    costumeThemeSchemeSurface: ComposeColor = ComposeColor(0xFF0E0E12),
    costumeThemeSchemeBackground: ComposeColor = ComposeColor(0xFF0E0E12),
    costumeThemeSchemePrimary: ComposeColor = ComposeColor(0xFF0E0E12),
    enableM3E: Boolean = false,
    lightThemeName: String,
    darkThemeName: String,
    appLocale: String,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val systemDark = isSystemInDarkTheme()

    val isDark = remember(darkMode, systemDark) {
        when (darkMode) {
            "Enabled" -> true
            "Disabled" -> false
            else -> systemDark
        }
    }


    val lightColorScheme = remember(lightThemeName, isDynamicColor) {
        if (isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            dynamicLightColorScheme(context)
        else when (lightThemeName) {
            "light_default" -> DefaultLightColorScheme
            "light_designer" -> DesignerLightColorScheme
            else -> DefaultLightColorScheme
        }
    }

    val darkColorScheme = remember(darkThemeName, isDynamicColor) {
        if (isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val dyn = dynamicDarkColorScheme(context)
            dyn
        } else when (darkThemeName) {
            "dark_obsidian" -> DarkObsidianColorScheme
            "dark_designer" -> DesignerDarkColorScheme
            else -> DefaultDarkColorScheme
        }
    }

    val costumeColorScheme = remember(isDynamicColor, isDark, darkThemeName, lightThemeName, costumeThemeSchemeSurface, costumeThemeSchemeBackground, costumeThemeSchemePrimary) {
        val scheme = if (isDark && isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context)
        } else if (!isDark && isDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicLightColorScheme(context)
        } else if (isDark && Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
            DarkObsidianColorScheme
        } else {
            DefaultLightColorScheme
        }
        scheme.copy(
            surface = costumeThemeSchemeSurface,
            background = costumeThemeSchemeBackground,
            primary = costumeThemeSchemePrimary
        )
    }

    val colorScheme = when {
        enableCostumeThemeScheme -> costumeColorScheme
        isDark -> darkColorScheme
        else -> lightColorScheme
    }

    val appTheme = remember(isDark, colorScheme) {
        AppTheme(isDark = isDark, colorScheme = colorScheme)
    }

    val textLocaleList = remember(appLocale) {
        appLocaleToTextLocaleList(appLocale)
    }

    @Suppress("deprecation")
    DisposableEffect(view, isDark) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)

        controller.isAppearanceLightStatusBars = !isDark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            controller.isAppearanceLightNavigationBars = !isDark
        }
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        onDispose { }
    }

    CompositionLocalProvider(
        LocalAppTheme provides appTheme,
        LocalLightColorScheme provides lightColorScheme,
        LocalDarkColorScheme provides darkColorScheme,
        LocalTextLocaleList provides textLocaleList
    ) {
        if (enableM3E) {
            MaterialExpressiveTheme(
                colorScheme = colorScheme,
                typography = AppTypography,
                content = content
            )
        } else {
            MaterialTheme(
                colorScheme = colorScheme,
                typography = AppTypography,
                content = content
            )
        }
    }
}
