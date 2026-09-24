package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import indi.dmzz_yyhyy.lightnovelreader.data.setting.AbstractSettingState
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope

@Suppress("MemberVisibilityCanBePrivate")
@Stable
class ThemeSettingState(
    userDataRepository: UserDataRepository,
    coroutineScope: CoroutineScope
) : AbstractSettingState(coroutineScope) {
    val darkModeUserData =
        userDataRepository.stringUserData(UserDataPath.Settings.Display.DarkMode.path)
    val dynamicColorsUserData =
        userDataRepository.booleanUserData(UserDataPath.Settings.Display.DynamicColors.path)
    val lightThemeNameUserData =
        userDataRepository.stringUserData(UserDataPath.Settings.Display.LightThemeName.path)
    val darkThemeNameUserData =
        userDataRepository.stringUserData(UserDataPath.Settings.Display.DarkThemeName.path)

    val darkMode by darkModeUserData.safeAsState("FollowSystem")
    val dynamicColors by dynamicColorsUserData.safeAsState(false)
    val lightThemeName by lightThemeNameUserData.safeAsState("light_default")
    val darkThemeName by darkThemeNameUserData.safeAsState("dark_default")
}
