package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import indi.dmzz_yyhyy.lightnovelreader.BuildConfig
import indi.dmzz_yyhyy.lightnovelreader.R
import kotlinx.coroutines.delay
import kotlin.math.round

@Composable
fun BaseDialog(
    icon: Painter,
    title: String,
    description: String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dismissText: String,
    confirmationText: String,
    confirmationEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    BaseDialog(
        icon = icon,
        title = title,
        description = description,
        onDismissRequest = onDismissRequest,
    ) {
        content.invoke(this)
        Row(
            modifier = Modifier
                .padding(8.dp, 24.dp, 24.dp, 24.dp)
                .align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = dismissText,
                    style = typography.labelMedium
                )
            }
            TextButton(
                onClick = onConfirmation,
                enabled = confirmationEnabled
            ) {
                Text(
                    text = confirmationText,
                    style = typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun BaseDialog(
    icon: Painter,
    title: String,
    description: String,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Card(
            modifier = Modifier
                .sizeIn(minWidth = 280.dp, maxWidth = 560.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceContainerHigh),
        ) {
            Box(Modifier.height(24.dp))
            Icon(
                modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                painter = icon,
                tint = colorScheme.secondary,
                contentDescription = null
            )
            Box(Modifier.height(16.dp))
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = title,
                style = typography.displayMedium,
                fontWeight = FontWeight.W500,
            )
            Box(Modifier.height(16.dp))
            Text(
                modifier = Modifier
                    .sizeIn(minWidth = 280.dp, maxWidth = 560.dp)
                    .padding(horizontal = 24.dp),
                textAlign = TextAlign.Start,
                text = description,
                style = typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
            Box(Modifier.height(16.dp))
            content.invoke(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliderDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onSlideChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    title: String,
    description: String
) {
    BaseDialog(
        icon = painterResource(R.drawable.filled_settings_24px),
        title = title,
        description = description,
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        dismissText = stringResource(R.string.cancel),
        confirmationText = stringResource(R.string.apply),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.book_word_count_limit),
                    style = typography.labelMedium
                )
                Spacer(Modifier.weight(1f))
                RollingNumber(
                    number = value.toInt(),
                    style = typography.labelMedium,
                    separator = true
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "+",
                    style = typography.labelMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            Slider(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                valueRange = valueRange,
                steps = steps,
                onValueChange = { newValue ->
                    val adjustedValue = if (steps > 0) {
                        val stepSize = (valueRange.endInclusive - valueRange.start) / (steps + 1)
                        valueRange.start + round((newValue - valueRange.start) / stepSize) * stepSize
                    } else {
                        newValue
                    }
                    onSlideChange(adjustedValue)
                },
                onValueChangeFinished = onSliderChangeFinished,
                colors = SliderDefaults.colors(
                    inactiveTrackColor = colorScheme.primaryContainer,
                )
            )
        }
    }
}


interface ExportContext {
    val localBookCache: Boolean
    val bookshelf: Boolean
    val readingData: Boolean
    val settings: Boolean
    val bookmark: Boolean
}

class MutableExportContext: ExportContext {
    override var localBookCache by mutableStateOf(true)
    override var bookshelf by mutableStateOf(true)
    override var readingData by mutableStateOf(true)
    override var settings by mutableStateOf(true)
    override var bookmark by mutableStateOf(true)
}

@Composable
fun ExportUserDataDialog(
    onDismissRequest: () -> Unit,
    onClickSaveAndSend: (ExportContext) -> Unit,
    onClickSaveToFile: (ExportContext) -> Unit
) {
    val mutableExportContext = remember { MutableExportContext() }
    val listItemModifier = Modifier
        .sizeIn(minWidth = 280.dp, maxWidth = 500.dp)
        .fillMaxWidth()
        .padding(horizontal = 14.dp)
    BaseDialog(
        icon = painterResource(R.drawable.output_24px),
        title = stringResource(R.string.settings_snap_data),
        description = stringResource(R.string.dialog_snap_user_data_text),
        onDismissRequest = onDismissRequest,
    ) {
        Column(Modifier.width(IntrinsicSize.Max).sizeIn(maxHeight = 350.dp)) {
            CheckBoxListItem(
                modifier = listItemModifier,
                title = stringResource(R.string.dialog_snap_local_book_cache),
                supportingText = stringResource(R.string.dialog_snap_local_book_cache_text),
                checked = mutableExportContext.localBookCache,
                onCheckedChange = { mutableExportContext.localBookCache = it }
            )
            HorizontalDivider(Modifier.padding(horizontal = 14.dp))
            CheckBoxListItem(
                modifier = listItemModifier,
                title = stringResource(R.string.dialog_snap_bookshelf),
                supportingText = stringResource(R.string.dialog_snap_bookshelf_text),
                checked = mutableExportContext.bookshelf,
                onCheckedChange = { mutableExportContext.bookshelf = it }
            )
            HorizontalDivider(Modifier.padding(horizontal = 14.dp))
            CheckBoxListItem(
                modifier = listItemModifier,
                title = stringResource(R.string.dialog_snap_reading_data),
                supportingText = stringResource(R.string.dialog_snap_reading_data_text),
                checked = mutableExportContext.readingData,
                onCheckedChange = { mutableExportContext.readingData = it }
            )
            HorizontalDivider(Modifier.padding(horizontal = 14.dp))
            CheckBoxListItem(
                modifier = listItemModifier,
                title = stringResource(R.string.dialog_snap_settings),
                supportingText = stringResource(R.string.dialog_snap_settings_text),
                checked = mutableExportContext.settings,
                onCheckedChange = { mutableExportContext.settings = it }
            )
            /*HorizontalDivider(Modifier.padding(horizontal = 14.dp))
            CheckBoxListItem(
                modifier = listItemModifier,
                title = stringResource(R.string.dialog_snap_bookmarks),
                supportingText = stringResource(R.string.dialog_snap_bookmarks_text),
                checked = mutableExportContext.bookmark,
                onCheckedChange = { mutableExportContext.bookmark = it }
            )
            HorizontalDivider(Modifier.padding(horizontal = 14.dp))*/
        }
        Row(
            modifier = Modifier
                .padding(8.dp, 24.dp, 24.dp, 24.dp)
                .align(Alignment.End),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = typography.labelMedium,
                    color = colorScheme.primary,
                )
            }
            TextButton(
                onClick = { onClickSaveAndSend(mutableExportContext) }
            ) {
                Text(
                    text = stringResource(R.string.export_and_share),
                    style = typography.labelMedium,
                    color = colorScheme.primary,
                )
            }
            TextButton(
                onClick = { onClickSaveToFile(mutableExportContext) }
            ) {
                Text(
                    text = stringResource(R.string.export_to_file),
                    style = typography.labelMedium,
                    color = colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun ImportUserDataDialog(
    isImporting: Boolean,
    onDismissRequest: () -> Unit,
    onClickMerge: () -> Unit,
    onClickOverwrite: () -> Unit,
) {

    var confirmingOverwrite by remember { mutableStateOf(false) }
    var countdown by remember { mutableIntStateOf(3) }

    LaunchedEffect(confirmingOverwrite) {
        if (confirmingOverwrite) {
            countdown = 3
            repeat(3) {
                delay(1000)
                countdown--
            }
        }
    }

    AlertDialog(
        onDismissRequest = if (isImporting) ({}) else onDismissRequest,
        icon = {
            Icon(
                painter = painterResource(R.drawable.input_24px),
                contentDescription = null,
                tint = colorScheme.secondary
            )
        },
        title = {
            Text(
                text = stringResource(
                    if (isImporting) R.string.import_in_progress_title
                    else R.string.import_data_dialog_title
                ),
                style = typography.headlineSmall
            )
        },
        text = {
            if (isImporting) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.import_in_progress_desc),
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.import_data_dialog_description),
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    ImportOptionTile(
                        icon = R.drawable.alt_route_24px,
                        title = stringResource(R.string.import_merge),
                        desc = stringResource(R.string.import_merge_desc),
                        containerColor = colorScheme.secondaryContainer,
                        iconColor = colorScheme.secondary,
                        textColor = colorScheme.onSecondaryContainer,
                        onClick = onClickMerge
                    )
                    ImportOptionTile(
                        icon = R.drawable.delete_forever_24px,
                        title = stringResource(R.string.import_overwrite),
                        desc = stringResource(R.string.import_overwrite_desc),
                        containerColor = colorScheme.errorContainer,
                        iconColor = colorScheme.error,
                        textColor = colorScheme.onErrorContainer,
                        onClick = { confirmingOverwrite = true }
                    )
                    if (confirmingOverwrite) {
                        Text(
                            text = stringResource(R.string.import_overwrite_warning),
                            style = typography.bodyMedium,
                            color = colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isImporting) return@AlertDialog
            if (confirmingOverwrite) {
                Button(
                    onClick = onClickOverwrite,
                    enabled = countdown == 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.error
                    )
                ) {
                    val confirmString = stringResource(R.string.import_overwrite_confirm)
                    Text(
                        if (countdown > 0) "$confirmString ($countdown)"
                        else confirmString
                    )
                }
            }
        },
        dismissButton = {
            if (isImporting) return@AlertDialog
            if (confirmingOverwrite) {
                TextButton(
                    onClick = { confirmingOverwrite = false }
                ) {
                    Text(stringResource(R.string.import_back))
                }
            } else {
                TextButton(
                    onClick = onDismissRequest
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
}

@Composable
private fun ImportOptionTile(
    icon: Int,
    title: String,
    desc: String,
    containerColor: Color,
    iconColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = iconColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = colorScheme.onPrimary
                    )
                }
            }

            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = typography.titleMedium,
                    color = textColor
                )
                Text(
                    desc,
                    style = typography.bodySmall,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun SettingsAboutInfoDialog(
    onDismissRequest: () -> Unit,
) {
    AlertDialog (
        onDismissRequest = onDismissRequest,
        text = {
            Column {
                Row {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        color = colorResource(id = R.color.ic_launcher_background),
                        shape = CircleShape
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icon_foreground),
                            contentDescription = "appIcon",
                            modifier = Modifier.scale(1.4f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {

                        Text(
                            stringResource(id = R.string.app_name),
                            style = typography.displayMedium
                        )
                        Text(
                            BuildConfig.APPLICATION_ID,
                            style = typography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = stringResource(R.string.settings_about_oss),
                    style = typography.labelLarge
                )
                Spacer(Modifier.height(10.dp))
                AnnotatedText(
                    text = stringResource(
                        id = R.string.settings_about_source_code,
                        "<b><a href=\"https://github.com/dmzz-yyhyy/LightNovelReader\">GitHub</a></b>",
                        "<b><a href=\"https://github.com/dmzz-yyhyy/LightNovelReader/issues\">GitHub Issues</a></b>"
                    ),
                    style = typography.labelLarge
                )

                Spacer(modifier = Modifier.height(18.dp))

                val titleColor = colorScheme.onSurface
                val contentColor = colorScheme.onSurfaceVariant
                Column {
                    Text(
                        stringResource(R.string.dialog_about_version), color = titleColor
                    )
                    Text(
                        "${BuildConfig.VERSION_NAME} [${BuildConfig.VERSION_CODE}]", color = contentColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.translators), color = titleColor
                    )
                    Text(
                        stringResource(R.string.language_translators), color = contentColor
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        },
        confirmButton = {},
    )
}

@Composable
fun SettingsDisableStatsDialog(
    onClickConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    onClickShowPrivacyPolicy: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(
            text = stringResource(R.string.settings_statistics_disable_dialog_title),
            style = typography.titleLarge
        ) },
        text = { 
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.settings_statistics_disable_dialog_text))
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = onClickShowPrivacyPolicy,
                    modifier = Modifier.align(Alignment.Start),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.privacy_policy),
                        color = colorScheme.primary,
                        style = typography.labelMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onClickConfirm()
                }
            ) {
                Text(
                    text = stringResource(R.string.settings_statistics_disable_dialog_confirm),
                    color = colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun SettingsPrivacyPolicyDialog(
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { 
            Text(
                text = stringResource(R.string.privacy_policy_title),
                style = typography.titleLarge
            ) 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.privacy_policy_collect_title),
                    style = typography.titleMedium,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.privacy_policy_collect_items),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = stringResource(R.string.privacy_policy_not_collect_title),
                    style = typography.titleMedium,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.privacy_policy_not_collect_items),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = stringResource(R.string.privacy_policy_commitment_title),
                    style = typography.titleMedium,
                    color = colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.privacy_policy_commitment_items),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(android.R.string.ok))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (Color) -> Unit,
    selectedColor: Color,
    colors: List<Color>,
) {
    var currentColor by remember {
        mutableStateOf(selectedColor)
    }

    BaseDialog (
        icon = painterResource(R.drawable.palette_24px),
        title = stringResource(R.string.dialog_color_picker),
        description = stringResource(R.string.dialog_color_picker_desc),
        onDismissRequest = onDismissRequest,
        onConfirmation = { onConfirmation(currentColor) },
        dismissText = stringResource(R.string.cancel),
        confirmationText = stringResource(R.string.apply),
    ) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            colors.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable {
                            currentColor = color
                        }
                ) {
                    val secondary = colorScheme.secondary
                    val surfaceContainer = colorScheme.surfaceContainer
                    val blockIconId = painterResource(R.drawable.block_24px)
                    Canvas(
                        modifier = Modifier.size(44.dp)
                    ) {
                        if (color == currentColor)
                            drawCircle(
                                color = secondary,
                                radius = 22.dp.toPx(),
                            )
                        drawCircle(
                            color = surfaceContainer,
                            radius = 20.dp.toPx(),
                        )
                        drawCircle(
                            color = if (color.isUnspecified) surfaceContainer else color,
                            radius = 20.dp.toPx(),
                        )
                    }
                    if (index == 0)
                        Icon(
                            modifier = Modifier.align(Alignment.Center),
                            painter = blockIconId,
                            contentDescription = null
                        )
                }
            }
        }
    }
}

@Composable
fun SliderValueDialog(
    value: Float,
    onValueChange: (Float) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmation: (Float) -> Unit
) {
    var text by remember { mutableStateOf(value.toString()) }

    LaunchedEffect(value) {
        text = value.toString()
    }

    val parsed = text.toFloatOrNull()
    val error = text.isNotBlank() && parsed == null

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.dialog_slider_custom)) },
        text = {
            Column {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Value") },
                    isError = error,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                )
                if (error) {
                    Text(
                        text = stringResource(R.string.dialog_slider_custom_illegal_value),
                        color = colorScheme.error,
                        style = typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    parsed?.let {
                        onValueChange(it)
                        onConfirmation(it)
                    }
                },
                enabled = parsed != null
            ) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun DeleteBookshelfDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit) {
    AlertDialog(
        title = {
            Text(
                text = stringResource(R.string.dialog_delete_bookshelf),
                style = typography.displayMedium,
                color = colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.dialog_delete_bookshelf_text),
                style = typography.bodyMedium,
                color = colorScheme.onSurfaceVariant
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirmation
            ) {
                Text(
                    text = stringResource(android.R.string.ok)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = stringResource(R.string.cancel)
                )
            }
        }
    )
}
