package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.list

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalNavigator
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.theme.navigateToSettingsAppThemeDestination
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.formats.navigateToSettingsFormatsDestination
import io.nightfish.lightnovelreader.api.ui.components.SettingsClickableEntry

@Composable
fun DisplaySettingsList() {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val isAboveTiramisu = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.format_paint_24px),
        title = stringResource(R.string.settings_theme),
        description = stringResource(R.string.settings_theme_desc),
        onClick = navigator::navigateToSettingsAppThemeDestination
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.language_24px),
        title = stringResource(R.string.settings_app_language),
        description = if (isAboveTiramisu)
            stringResource(R.string.settings_app_language_desc)
        else stringResource(R.string.settings_app_language_desc_unavailable),
        option = if (isAboveTiramisu) stringResource(R.string.language)
        else stringResource(R.string.follow_system),
        onClick = {
            if (isAboveTiramisu) {
                val intent = Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                intent.data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(intent)
            } else return@SettingsClickableEntry
        }
    )
    SettingsClickableEntry(
        modifier = Modifier.background(colorScheme.surfaceContainer),
        painter = painterResource(R.drawable.short_text_24px),
        title = stringResource(R.string.settings_formats),
        description = stringResource(R.string.settings_formats_desc),
        onClick = navigator::navigateToSettingsFormatsDestination
    )
}