package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsMenuEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsSwitchEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions

@Composable
fun AppSettingsList(
    settingState: SettingState,
    onClickLogcat: () -> Unit,
    onClickPluginManager: () -> Unit
) {
    SettingsClickableEntry(
        iconRes = R.drawable.extension_24px,
        title = "扩展插件",
        description = "安装和管理扩展插件",
        onClick = onClickPluginManager,
        option = stringResource(R.string.item_view_details)
    )
    SettingsSwitchEntry(
        iconRes = R.drawable.wifi_proxy_24px,
        title = "自动代理",
        description = "自动从网络上获取代理ip, <b>但这将导致您所浏览的书本数据可能会被截获</b>",
        checked = settingState.isUseProxy,
        booleanUserData = settingState.isUseProxyUserData
    )
    SettingsClickableEntry(
        iconRes = R.drawable.bug_report_24px,
        title = stringResource(R.string.settings_app_logs),
        description = stringResource(R.string.settings_app_logs_desc),
        onClick = onClickLogcat
    )
    SettingsMenuEntry(
        iconRes = R.drawable.bug_report_24px,
        title = stringResource(R.string.settings_app_log_level),
        description = stringResource(R.string.settings_app_log_level_desc),
        options = MenuOptions.LogLevelOptions,
        selectedOptionKey = settingState.logLevelKey,
        onOptionChange = settingState.logLevelKeyUserData::asynchronousSet
    )
}