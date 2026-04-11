package indi.dmzz_yyhyy.lightnovelreader.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * 自定義主題色彩預設結構
 * 包含 18 個 Material 3 色彩角色
 */
data class CostumeThemeColorPreset(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val error: Color,
    val onError: Color,
)

/**
 * 深色模式的自定義主題色彩預設
 * 用於 isDark = true 時的基礎深色方案
 */
val CostumeThemeColorsDark = CostumeThemeColorPreset(
    primary = Color(0xFFC6C2EE),
    onPrimary = Color(0xFF3E3C60),
    primaryContainer = Color(0xFF514E74),
    onPrimaryContainer = Color(0xFFE4E0FF),
    secondary = Color(0xFFC7C4DC),
    onSecondary = Color(0xFF403E52),
    secondaryContainer = Color(0xFF3B394D),
    onSecondaryContainer = Color(0xFFC0BCD5),
    tertiary = Color(0xFFFFDEFC),
    onTertiary = Color(0xFF6B496C),
    tertiaryContainer = Color(0xFFF8CCF6),
    onTertiaryContainer = Color(0xFF624164),
    background = Color(0xFF0E0E12),
    onBackground = Color(0xFFE8E4F0),
    surface = Color(0xFF0E0E12),
    onSurface = Color(0xFFE8E4F0),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
)

/**
 * 淺色模式的自定義主題色彩預設
 * 用於 isDark = false 時的基礎顏色方案
 * 色彩對應於深色預設的淺色倒轉版本
 */
val CostumeThemeColorsLight = CostumeThemeColorPreset(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
)

val DefaultLightColorScheme = lightColorScheme()

val DefaultDarkColorScheme = darkColorScheme()

val DarkObsidianColorScheme = darkColorScheme(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
)

val DesignerLightColorScheme = lightColorScheme(
    primary = Color(0xFF5D5A8B),
    onPrimary = Color(0xFFFCF7FF),
    primaryContainer = Color(0xFFCAC6FF),
    onPrimaryContainer = Color(0xFF413E6E),
    secondary = Color(0xFF5F5D72),
    onSecondary = Color(0xFFFCF7FF),
    secondaryContainer = Color(0xFFE4DFF9),
    onSecondaryContainer = Color(0xFF514F64),
    tertiary = Color(0xFF765377),
    onTertiary = Color(0xFFFFF7FA),
    tertiaryContainer = Color(0xFFF8CCF6),
    onTertiaryContainer = Color(0xFF624164),
    background = Color(0xFFFCF8FE),
    onBackground = Color(0xFF32313A),
    surface = Color(0xFFFCF8FE),
    onSurface = Color(0xFF32313A),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF)
)

val DesignerDarkColorScheme = darkColorScheme(
    primary = Color(0xFFC6C2EE),
    onPrimary = Color(0xFF3E3C60),
    primaryContainer = Color(0xFF514E74),
    onPrimaryContainer = Color(0xFFE4E0FF),
    secondary = Color(0xFFC7C4DC),
    onSecondary = Color(0xFF403E52),
    secondaryContainer = Color(0xFF3B394D),
    onSecondaryContainer = Color(0xFFC0BCD5),
    tertiary = Color(0xFFFFDEFC),
    onTertiary = Color(0xFF6B496C),
    tertiaryContainer = Color(0xFFF8CCF6),
    onTertiaryContainer = Color(0xFF624164),
    background = Color(0xFF0E0E12),
    onBackground = Color(0xFFE8E4F0),
    surface = Color(0xFF0E0E12),
    onSurface = Color(0xFFE8E4F0),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410)
)

// 預設顏色列表補充
val DEFAULT_COLOR_PRESETS = listOf(
    Color.Unspecified,
    // 紫色系
    Color(0xFF6750A4),
    Color(0xFF625B71),
    Color(0xFF7D5260),
    Color(0xFFC6C2EE),
    Color(0xFFC7C4DC),
    Color(0xFFFFDEFC),
    // 紅色系
    Color(0xFFB3261E),
    Color(0xFFF2B8B5),
    Color(0xFFFF6B6B),
    Color(0xFFD32F2F),
    // 橙色系
    Color(0xFFFF9800),
    Color(0xFFFFB74D),
    Color(0xFFFFD54F),
    // 綠色系
    Color(0xFF4CAF50),
    Color(0xFF81C784),
    Color(0xFFC8E6C9),
    // 青色系
    Color(0xFF00BCD4),
    Color(0xFF4DD0E1),
    Color(0xFFB2EBF2),
    // 藍色系
    Color(0xFF2196F3),
    Color(0xFF64B5F6),
    Color(0xFFBBDEFB),
    // 灰色系
    Color(0xFF757575),
    Color(0xFFBDBDBD),
    Color(0xFFE0E0E0),
    // 黑白
    Color(0xFF000000),
    Color(0xFFFFFFFF),
)