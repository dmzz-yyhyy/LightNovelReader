package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToColorPickerDialog
import indi.dmzz_yyhyy.lightnovelreader.utils.popBackStackIfResumed
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import io.nightfish.lightnovelreader.api.userdata.UserDataPath

fun NavGraphBuilder.settingsThemeDestination() {
    composable<Route.Main.Settings.Theme> {
        val navController = LocalNavController.current
        val viewModel = hiltViewModel<ThemeViewModel>()
        val readerSettingState = viewModel.settingState
        val isDark = LocalAppTheme.current.isDark
        ThemeScreen(
            themeSettingState = readerSettingState,
            onClickBack = navController::popBackStackIfResumed,
            onClickChangeTextColor = {
                navController.navigateToColorPickerDialog(
                    if (isDark) UserDataPath.Reader.TextDarkColor.path
                    else UserDataPath.Reader.TextColor.path,
                    listOf(-1, 0xFF1D1B20, 0xFFE6E0E9),
                    target = Route.Book.ColorPickerTargetType.TEXT,
                )
            },
            onClickChangeBackgroundColor = {
                navController.navigateToColorPickerDialog(
                    if (isDark) UserDataPath.Reader.BackgroundDarkColor.path
                    else UserDataPath.Reader.BackgroundColor.path,
                    listOf(
                        -1,
                        0x38E8CCA5,
                        0x38FF8080,
                        0x38d3b17d,
                        0x3834C759,
                        0x3832ADE6,
                        0x38007AFF,
                        0x385856D6,
                        0x38AF52DE
                    ),
                    target = Route.Book.ColorPickerTargetType.BACKGROUND,
                )
            },
        )
    }
}

fun NavController.navigateToSettingsThemeDestination() {
    navigate(Route.Main.Settings.Theme)
}

