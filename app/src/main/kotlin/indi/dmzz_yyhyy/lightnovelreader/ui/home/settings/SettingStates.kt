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
    val checkUpdateUserData = userDataRepository.booleanUserData(UserDataPath.Settings.App.AutoCheckUpdate.path)
    val appLocaleKeyUserData = userDataRepository.stringUserData(UserDataPath.Settings.Display.AppLocale.path)
    val statisticsUserData = userDataRepository.booleanUserData(UserDataPath.Settings.App.Statistics.path)
    val updateChannelKeyUserData = userDataRepository.stringUserData(UserDataPath.Settings.App.UpdateChannel.path)
    val distributionPlatformKeyUserData = userDataRepository.stringUserData(UserDataPath.Settings.App.DistributionPlatform.path)
    val logLevelKeyUserData = userDataRepository.stringUserData(UserDataPath.Settings.Data.LogLevel.path)
    val isUseProxyUserData = userDataRepository.booleanUserData(UserDataPath.Settings.Data.IsUseProxy.path)
    val enableSimplifiedTraditionalTransformUserData = userDataRepository.booleanUserData(
        UserDataPath.Reader.EnableSimplifiedTraditionalTransform.path)

    val checkUpdate by checkUpdateUserData.asState(true)
    val appLocaleKey by appLocaleKeyUserData.asState("zh-CN")
    val statistics by statisticsUserData.asState(true)
    val updateChannelKey by updateChannelKeyUserData.asState("Development")
    val distributionPlatformKey by distributionPlatformKeyUserData.asState("LnrAPI")
    val logLevelKey by logLevelKeyUserData.asState("none")
    val isUseProxy by isUseProxyUserData.asState(false)
    val enableSimplifiedTraditionalTransform by enableSimplifiedTraditionalTransformUserData.safeAsState(false)
}