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
        userDataRepository.userData(UserDataPath.Settings.Display.DarkMode)
    val dynamicColorsUserData =
        userDataRepository.userData(UserDataPath.Settings.Display.DynamicColors)
    val lightThemeNameUserData =
        userDataRepository.userData(UserDataPath.Settings.Display.LightThemeName)
    val darkThemeNameUserData =
        userDataRepository.userData(UserDataPath.Settings.Display.DarkThemeName)

    val darkMode by darkModeUserData.safeAsState("FollowSystem")
    val dynamicColors by dynamicColorsUserData.safeAsState(false)
    val lightThemeName by lightThemeNameUserData.safeAsState("light_default")
    val darkThemeName by darkThemeNameUserData.safeAsState("dark_default")
}
