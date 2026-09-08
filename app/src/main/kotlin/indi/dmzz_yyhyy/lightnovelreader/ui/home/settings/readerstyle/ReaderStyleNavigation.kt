package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.readerstyle

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.navigateToColorPickerDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.NavEntryScope
import indi.dmzz_yyhyy.lightnovelreader.ui.navigation.Navigator
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.userdata.UserDataPath

fun NavEntryScope.settingsReaderStyleDestination() {
    entry<Route.Main.Settings.ReaderStyle> {
        val navigator = LocalNavigator.current
        val viewModel = hiltViewModel<ReaderStyleViewModel>()
        val readerSettingState = viewModel.settingState
        val isDark = LocalAppTheme.current.isDark
        ReaderStyleScreen(
            settingState = readerSettingState,
            onClickBack = navigator::popBackStack,
            onClickChangeTextColor = {
                navigator.navigateToColorPickerDialog(
                    if (isDark) UserDataPath.Reader.TextDarkColor.path
                    else UserDataPath.Reader.TextColor.path,
                    listOf(-1, 0xFF1D1B20, 0xFFE6E0E9),
                    target = Route.Book.ColorPickerTargetType.TEXT,
                )
            },
            onClickChangeBackgroundColor = {
                navigator.navigateToColorPickerDialog(
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

fun Navigator.navigateToSettingsReaderStyleDestination() {
    navigate(Route.Main.Settings.ReaderStyle)
}
