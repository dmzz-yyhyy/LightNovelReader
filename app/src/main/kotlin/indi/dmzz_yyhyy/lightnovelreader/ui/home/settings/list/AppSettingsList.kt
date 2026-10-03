package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import io.nightfish.lightnovelreader.api.ui.components.SettingsClickableEntry
import io.nightfish.lightnovelreader.api.ui.components.SettingsMenuEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions

@Composable
fun AppSettingsList(
    settingState: SettingState,
    onClickLogcat: () -> Unit,
) {
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.bug_report_24px),
        title = stringResource(R.string.settings_app_logs),
        description = stringResource(R.string.settings_app_logs_desc),
        onClick = onClickLogcat
    )
    SettingsMenuEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.bug_report_24px),
        title = stringResource(R.string.settings_app_log_level),
        description = stringResource(R.string.settings_app_log_level_desc),
        options = MenuOptions.LogLevelOptions,
        selectedOptionKey = settingState.logLevelKey,
        onOptionChange = { option ->
            settingState.logLevelKeyUserData.asynchronousSet(option)
        }
    )
}