@file:Suppress("AssignedValueIsNeverRead")

package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.BuildConfig
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsAboutInfoDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsClickableEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsDisableStatsDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SettingsPrivacyPolicyDialog
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState
import io.nightfish.lightnovelreader.api.ui.components.SettingsSwitchEntry

@Composable
fun AboutSettingsList(
    settingState: SettingState,
    onClickLicenses: () -> Unit
) {
    val appInfo: String = buildString {
        appendLine(BuildConfig.APPLICATION_ID)
        append("${BuildConfig.VERSION_NAME} [${BuildConfig.VERSION_CODE}] - ")
            .append(if (BuildConfig.DEBUG) "debug" else "release")
    }
    var showAppInfoDialog by remember { mutableStateOf(false) }
    var showDisableStatsDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    if (showAppInfoDialog) {
        SettingsAboutInfoDialog(onDismissRequest = { showAppInfoDialog = false })
    }

    if (showPrivacyPolicy) {
        SettingsPrivacyPolicyDialog(
            onDismissRequest = {
                showPrivacyPolicy = false
                showDisableStatsDialog = false
            }
        )
    }

    if (showDisableStatsDialog) {
        SettingsDisableStatsDialog(
            onClickConfirm = {
                settingState.statisticsUserData.asynchronousSet(false)
                showDisableStatsDialog = false
            },
            onDismissRequest = { showDisableStatsDialog = false },
            onClickShowPrivacyPolicy = {
                showPrivacyPolicy = true
            }
        )
    }

    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.info_24px),
        title = stringResource(R.string.app_name),
        description = appInfo,
        onClick = { showAppInfoDialog = true },
        option = stringResource(R.string.item_view_details)
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.group_24px),
        title = stringResource(R.string.settings_communication),
        description = stringResource(R.string.settings_communication_desc),
        openUrl = "https://qm.qq.com/q/Tp80Hf9Oms"
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.archive_24px),
        title = stringResource(R.string.settings_github_repo),
        description = stringResource(R.string.settings_github_repo_desc),
        openUrl = "https://github.com/dmzz-yyhyy/LightNovelReader"
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.volunteer_activism_24px),
        title = stringResource(R.string.settings_support_author),
        description = stringResource(R.string.settings_support_author_desc),
        openUrl = "https://afdian.com/a/lightnovelreader"
    )
    SettingsSwitchEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable. data_usage_24px),
        title = stringResource(R.string.settings_statistics),
        description = stringResource(R.string.settings_statistics_desc),
        checked = if (BuildConfig.DEBUG) false else settingState.statistics,
        onCheckedChange = { checked ->
            if (!checked && settingState.statistics) {
                showDisableStatsDialog = true
            } else {
                settingState.statisticsUserData.asynchronousSet(checked)
            }
        },
        disabled = BuildConfig.DEBUG
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.code_24px),
        title = stringResource(R.string.settings_open_source_licenses),
        onClick = onClickLicenses
    )
}