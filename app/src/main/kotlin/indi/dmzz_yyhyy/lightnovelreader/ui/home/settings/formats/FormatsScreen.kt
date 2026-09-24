package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.formats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SectionDescription
import io.nightfish.lightnovelreader.api.ui.components.SettingsMenuEntry
import io.nightfish.lightnovelreader.api.ui.components.SettingsSwitchEntry
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.SettingsCategory
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.DateFormat
import indi.dmzz_yyhyy.lightnovelreader.utils.DateOrder
import indi.dmzz_yyhyy.lightnovelreader.utils.dateFormatter
import indi.dmzz_yyhyy.lightnovelreader.utils.formTime
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun FormatsScreen(
    settingState: SettingState,
    onClickBack: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize()) {
        item { TopBar(onClickBack) }
        item { SettingsList(settingState) }
    }
}

@Composable
fun SettingsList(settingState: SettingState) {
    val exampleDate = LocalDate.of(2026, 3, 10)
    val exampleRelativeTime = remember { LocalDateTime.now().minusMinutes(90) }
    val dateFormat = DateFormat.fromString(settingState.dateFormat)
    val dateOrder = DateOrder.fromString(settingState.dateOrder)

    val dateDescription =
        exampleDate.format(dateFormatter(dateFormat, settingState.dateShowYear, dateOrder))
    val relativeDescription = formTime(
        exampleRelativeTime,
        dateFormat,
        settingState.useRelativeTime
    )

    SettingsCategory(
        title = stringResource(R.string.date_time_formats_settings),
    ) {
        SettingsMenuEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            title = stringResource(R.string.settings_formats_date_format),
            description = dateDescription,
            options = MenuOptions.DateFormatOptions,
            selectedOptionKey = settingState.dateFormat,
            stringUserData = settingState.dateFormatUserData
        )
        SettingsSwitchEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            title = stringResource(R.string.settings_formats_date_show_year),
            description = stringResource(R.string.settings_formats_date_show_year_desc),
            checked = settingState.dateShowYear,
            booleanUserData = settingState.dateShowYearUserData
        )
        val isDateOrderEnabled = dateFormat == DateFormat.NUMERIC
        SettingsMenuEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            title = stringResource(R.string.settings_formats_date_order),
            description = stringResource(R.string.settings_formats_date_order_desc),
            options = MenuOptions.DateOrderOptions,
            selectedOptionKey = if (isDateOrderEnabled) settingState.dateOrder
            else MenuOptions.DateOrderOptions.Auto,
            stringUserData = settingState.dateOrderUserData,
            enabled = isDateOrderEnabled
        )
        SettingsSwitchEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            title = stringResource(R.string.settings_formats_relative_time_format),
            description = relativeDescription,
            checked = settingState.useRelativeTime,
            booleanUserData = settingState.useRelativeTimeUserData
        )
    }

    SettingsCategory(
        title = stringResource(R.string.settings_advanced),
    ) {
        SettingsMenuEntry(
            modifier = Modifier.background(colorScheme.surfaceContainer),
            painter = painterResource(R.drawable.translate_24px),
            title = stringResource(R.string.settings_characters_variant),
            description = stringResource(R.string.settings_characters_variant_desc),
            options = MenuOptions.AppLocaleOptions,
            selectedOptionKey = settingState.appLocaleKey,
            onOptionChange = settingState.appLocaleKeyUserData::asynchronousSet
        )
    }

    SectionDescription(
        modifier = Modifier.padding(horizontal = 24.dp),
        text = stringResource(R.string.settings_formats_characters_variant_note)
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(R.string.settings_formats))
            }
        },
        navigationIcon = {
            IconButton(onClickBack) {
                Icon(
                    painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
    )
}