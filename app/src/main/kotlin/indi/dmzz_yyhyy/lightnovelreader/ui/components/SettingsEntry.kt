@file:Suppress("AssignedValueIsNeverRead")

package indi.dmzz_yyhyy.lightnovelreader.ui.components

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.navigateToSliderValueDialog
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import io.nightfish.lightnovelreader.api.userdata.BooleanUserData
import io.nightfish.lightnovelreader.api.userdata.FloatUserData
import io.nightfish.lightnovelreader.api.userdata.StringUserData
import java.text.DecimalFormat
import kotlin.math.roundToInt

@Composable
fun SettingsSwitchEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    description: String,
    checked: Boolean,
    booleanUserData: BooleanUserData,
    disabled: Boolean = false
) {
    SettingsSwitchEntry(
        modifier = modifier,
        painter = painter,
        title = title,
        description = description,
        checked = checked,
        onCheckedChange = booleanUserData::asynchronousSet,
        disabled = disabled
    )
}

@Composable
fun SettingsSwitchEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    disabled: Boolean = false
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .then(modifier)
            .fillMaxWidth()
            .clickable(enabled = !disabled) { onCheckedChange(!checked) }
            .padding(horizontal = 22.dp)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        painter?.let {
            Icon(
                modifier = Modifier.padding(end = 22.dp).size(24.dp),
                painter = it,
                tint = colorScheme.onSurfaceVariant,
                contentDescription = "Icon"
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = colorScheme.onSurface,
                style = typography.headlineSmall
            )
            Text(
                text = description,
                color = colorScheme.onSurfaceVariant,
                style = typography.bodyMedium
            )
        }

        Box(
            modifier = Modifier
                .width(60.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Switch(
                checked = checked,
                enabled = !disabled,
                onCheckedChange = if (disabled) null else onCheckedChange
            )
        }
    }
}

@Composable
fun SettingsSliderEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    unit: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueFormat: (Float) -> Float = { (it * 2).roundToInt().toFloat() / 2 },
    floatUserData: FloatUserData,
    steps: List<Float>? = null
) {
    val navController = LocalNavController.current
    var tempValue by remember { mutableFloatStateOf(value) }
    LaunchedEffect(value) {
        tempValue = value
    }

    SettingsSliderEntry(
        painter = painter,
        modifier = modifier,
        title = title,
        unit = unit,
        value = tempValue,
        valueRange = valueRange,
        valueFormat = valueFormat,
        steps = steps,
        onSlideChange = { tempValue = it },
        onSliderChangeFinished = { floatUserData.asynchronousSet(tempValue) },
        onLongClick = {
            navController.navigateToSliderValueDialog(floatUserData.path, tempValue)
        }
    )
}

@Composable
private fun SettingsSliderEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    unit: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueFormat: (Float) -> Float = { (it * 2).roundToInt().toFloat() / 2 },
    steps: List<Float>? = null,
    onSlideChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    onLongClick: () -> Unit
) {
    val actualSteps = steps?.distinct()?.sorted()
    val sliderValue = if (actualSteps != null) {
        actualSteps.indexOfFirst { it == value }.takeIf { it >= 0 }?.toFloat()
            ?: actualSteps.indexOfFirst { it > value }.coerceAtLeast(0).toFloat()
    } else value

    val sliderRange = if (actualSteps != null) 0f..(actualSteps.lastIndex.toFloat())
    else valueRange

    val stepsCount = if (actualSteps != null) actualSteps.size - 2 else 0

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .then(modifier)
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            )
            .padding(horizontal = 22.dp)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        painter?.let {
            Icon(
                modifier = Modifier.padding(end = 22.dp).size(24.dp),
                painter = it,
                tint = colorScheme.onSurfaceVariant,
                contentDescription = "Icon"
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = colorScheme.onSurface,
                style = typography.headlineSmall
            )

            val displayValue = if (actualSteps != null)
                actualSteps[sliderValue.toInt()]
            else value

            Row(
                modifier = Modifier
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = FastOutSlowInEasing
                        )
                    )
            ) {
                AnimatedText(
                    text = "${DecimalFormat("#.#").format(displayValue)}",
                    color = colorScheme.primary,
                    style = typography.bodyMedium,
                    maxLines = 1
                )

                Spacer(Modifier.width(1.dp))

                Text(
                    text = unit,
                    color = colorScheme.primary,
                    style = typography.bodyMedium,
                    maxLines = 1
                )
            }

            Slider(
                modifier = Modifier.fillMaxWidth(),
                value = sliderValue,
                valueRange = sliderRange,
                steps = stepsCount,
                onValueChange = { raw ->
                    val newValue = if (actualSteps != null) {
                        val index = raw.roundToInt().coerceIn(0, actualSteps.lastIndex)
                        actualSteps[index]
                    } else {
                        valueFormat(raw)
                    }
                    onSlideChange(newValue)
                },
                onValueChangeFinished = onSliderChangeFinished,
                colors = SliderDefaults.colors(
                    inactiveTrackColor = colorScheme.primaryContainer,
                ),
            )
        }
    }
}


@Composable
fun SettingsMenuEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    description: String? = null,
    options: MenuOptions,
    selectedOptionKey: String,
    stringUserData: StringUserData
) {
    SettingsMenuEntry(
        modifier = modifier,
        painter = painter,
        title = title,
        description = description,
        options = options,
        selectedOptionKey = selectedOptionKey,
        onOptionChange = { stringUserData.asynchronousSet(it) }
    )
}

@Composable
fun SettingsMenuEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    description: String? = null,
    options: MenuOptions,
    selectedOptionKey: String,
    onOptionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember {
        mutableStateOf(options.getOrNull(selectedOptionKey))
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .then(modifier)
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 22.dp)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        painter?.let {
            Icon(
                modifier = Modifier.padding(end = 22.dp).size(24.dp),
                painter = it,
                tint = colorScheme.onSurfaceVariant,
                contentDescription = "Icon"
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = colorScheme.onSurface,
                style = typography.headlineSmall
            )
            description?.let {
                Text(
                    text = it,
                    color = colorScheme.onSurfaceVariant,
                    style = typography.bodyMedium
                )
            }
            AnimatedTextLine(
                text =
                    selectedOption?.let { stringResource(it.nameId) }
                        ?: "(null)",
                style = typography.bodyMedium,
                color = colorScheme.primary
            )

            Box(
                modifier = Modifier.offset {
                    IntOffset(0, 20)
                }
            ) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.optionList.forEach { option ->
                        DropdownMenuItem(
                            modifier = if (option.key == selectedOptionKey)
                                Modifier.background(colorScheme.surfaceContainerHighest)
                            else
                                Modifier,
                            onClick = {
                                selectedOption = option
                                onOptionChange(option.key)
                                expanded = false
                            },
                            enabled = true,
                            interactionSource = remember { MutableInteractionSource() },
                            text = {
                                Text(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    text = stringResource(option.nameId),
                                    style = typography.bodyLarge
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsClickableEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    description: String,
    openUrl: String
) {
    val context = LocalContext.current
    SettingsClickableEntry(
        modifier = modifier,
        painter = painter,
        title = title,
        description = description,
        onClick = {
            openUrl.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent, null)
            }
        }
    )
}

@Composable
fun SettingsClickableEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    option: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    description: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .then(modifier)
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 22.dp)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        painter?.let {
            Icon(
                modifier = Modifier.padding(end = 22.dp).size(24.dp),
                painter = it,
                tint = colorScheme.onSurfaceVariant,
                contentDescription = "Icon"
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = colorScheme.onSurface,
                style = typography.headlineSmall,
            )
            description?.let {
                Text(
                    text = it,
                    color = colorScheme.onSurfaceVariant,
                    style = typography.bodyMedium
                )
            }
            option?.let {
                AnimatedTextLine(
                    text = it,
                    style = typography.bodyMedium,
                    color = colorScheme.primary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        trailingContent?.let { composable ->
            Box(
                modifier = Modifier.fillMaxHeight()
                    .width(55.dp),
                contentAlignment = Alignment.Center
            ) {
                composable()
            }
        }
    }
}
