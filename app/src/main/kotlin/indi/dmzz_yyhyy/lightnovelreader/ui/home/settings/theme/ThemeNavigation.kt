package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.theme.DEFAULT_COLOR_PRESETS
import indi.dmzz_yyhyy.lightnovelreader.theme.DEFAULT_COLOR_PRESETS
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.ColorPickerDialogViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.ColorPickerDialogViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToColorPickerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToCostumeColorPickerDialog
import io.nightfish.lightnovelreader.api.Route
=======
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToCostumeColorPickerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Route
>>>>>>> d10c874c (自定意顏色移至CostumeThemeSchemeScreen (獨立設定畫面))
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import io.nightfish.lightnovelreader.api.userdata.UserDataPath

fun NavGraphBuilder.settingsThemeDestination() {
    composable<Route.Main.Settings.Theme.Main> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<ThemeViewModel>()
        val readerSettingState = viewModel.settingState
        val isDark = LocalAppTheme.current.isDark
        val colorPickerViewModel = hiltViewModel<ColorPickerDialogViewModel>()

        ThemeScreen(
            themeSettingState = readerSettingState,
            onClickBack = navController::popBackStackIfResumed,
            onClickChangeTextColor = {
                navController.navigateToColorPickerDialog(
                    if (isDark) UserDataPath.Reader.TextDarkColor.path
                    else UserDataPath.Reader.TextColor.path,
                    listOf(-1L, 0xFF1D1B20L, 0xFFE6E0E9L)
                )
            },
            onClickChangeBackgroundColor = {
                navController.navigateToColorPickerDialog(
                    if (isDark) UserDataPath.Reader.BackgroundDarkColor.path
                    else UserDataPath.Reader.BackgroundColor.path,
                    listOf(-1L, 0x38E8CCA5L, 0x38FF8080L, 0x38d3b17dL, 0x3834C759L, 0x3832ADE6L, 0x38007AFFL, 0x385856D6L, 0x38AF52DEL)
                )
            },
            onClickOpenCostumeThemeScheme = {
                navController.navigateToCostumeThemeSchemeScreen()
            },
        )
    }
    
    composable<Route.Main.Settings.Theme.CostumeThemeScheme> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<ThemeViewModel>()
        val readerSettingState = viewModel.settingState
        val isDark = LocalAppTheme.current.isDark
        
        CostumeThemeSchemeScreen(
            settingState = readerSettingState,
            onClickBack = navController::popBackStackIfResumed,
            onClickOpenColorPicker = { colorPath ->
                val colorList = listOf(-1L, 0x000000L, 0xffffffffL) + DEFAULT_COLOR_PRESETS.map {
                    if (it == Color.Unspecified) -1L else it.value.toLong()
                }
                navController.navigateToCostumeColorPickerDialog(colorPath, colorList)
            }
        )
    }
}

fun NavController.navigateToSettingsThemeDestination() {
    navigate(Route.Main.Settings.Theme.Main)
}

fun NavController.navigateToCostumeThemeSchemeScreen() {
    navigate(Route.Main.Settings.Theme.CostumeThemeScheme)
}
