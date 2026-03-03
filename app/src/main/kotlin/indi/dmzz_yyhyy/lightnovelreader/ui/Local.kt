package indi.dmzz_yyhyy.lightnovelreader.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import indi.dmzz_yyhyy.lightnovelreader.theme.AppTheme


val LocalAppTheme = staticCompositionLocalOf<AppTheme> {
    error("No AppTheme provided")
}

val LocalLightColorScheme = staticCompositionLocalOf<ColorScheme> {
    error("No Light ColorScheme provided")
}

val LocalDarkColorScheme = staticCompositionLocalOf<ColorScheme> {
    error("No Dark ColorScheme provided")
}

val LocalBottomBarController = staticCompositionLocalOf<(Boolean) -> Unit> {
    {}
}

val LocalImageHeaderGetter = staticCompositionLocalOf<() -> Map<String, String>> {
    error("No LocalImageHeaderGetter provided")
}
