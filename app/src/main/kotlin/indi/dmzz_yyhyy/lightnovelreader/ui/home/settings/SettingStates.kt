package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import indi.dmzz_yyhyy.lightnovelreader.data.setting.AbstractSettingState
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import kotlinx.coroutines.CoroutineScope

@Stable
class SettingState(
    userDataRepository: UserDataRepository,
    coroutineScope: CoroutineScope
) : AbstractSettingState(coroutineScope) {
    val checkUpdateUserData =
        userDataRepository.userData(UserDataPath.Settings.App.AutoCheckUpdate)
    val appLocaleKeyUserData =
        userDataRepository.userData(UserDataPath.Settings.Display.AppLocale)
    val statisticsUserData =
        userDataRepository.userData(UserDataPath.Settings.App.Statistics)
    val updateChannelKeyUserData =
        userDataRepository.userData(UserDataPath.Settings.App.UpdateChannel)
    val distributionPlatformKeyUserData =
        userDataRepository.userData(UserDataPath.Settings.App.DistributionPlatform)
    val logLevelKeyUserData =
        userDataRepository.userData(UserDataPath.Settings.Data.LogLevel)
    val isUseProxyUserData =
        userDataRepository.userData(UserDataPath.Settings.Data.IsUseProxy)
    val enableSimplifiedTraditionalTransformUserData =
        userDataRepository.userData(UserDataPath.Reader.EnableSimplifiedTraditionalTransform)
    val dateFormatUserData =
        userDataRepository.userData(UserDataPath.Settings.Display.DateStyle)
    val dateShowYearUserData =
        userDataRepository.userData(UserDataPath.Settings.Display.DateShowYear)
    val dateOrderUserData =
        userDataRepository.userData(UserDataPath.Settings.Display.DateOrder)
    val useRelativeTimeUserData =
        userDataRepository.userData(UserDataPath.Settings.Display.RelativeTimeStyle)

    val checkUpdate by checkUpdateUserData.asState(true)
    val appLocaleKey by appLocaleKeyUserData.asState("none")
    val statistics by statisticsUserData.asState(true)
    val updateChannelKey by updateChannelKeyUserData.asState("Development")
    val distributionPlatformKey by distributionPlatformKeyUserData.asState("LnrAPI")
    val logLevelKey by logLevelKeyUserData.asState("none")
    val isUseProxy by isUseProxyUserData.asState(false)
    val enableSimplifiedTraditionalTransform by enableSimplifiedTraditionalTransformUserData.safeAsState(
        false
    )
    val dateFormat by dateFormatUserData.safeAsState("numeric")
    val dateShowYear by dateShowYearUserData.asState(true)
    val dateOrder by dateOrderUserData.safeAsState("auto")
    val useRelativeTime by useRelativeTimeUserData.asState(true)
}
