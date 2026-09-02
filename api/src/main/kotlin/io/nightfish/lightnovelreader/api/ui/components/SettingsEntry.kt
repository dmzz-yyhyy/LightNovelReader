package io.nightfish.lightnovelreader.api.ui.components

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import io.nightfish.lightnovelreader.api.Route
import io.nightfish.lightnovelreader.api.settings.SettingsMenuOptionGroup
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import io.nightfish.lightnovelreader.api.userdata.BooleanUserData
import io.nightfish.lightnovelreader.api.userdata.FloatUserData
import io.nightfish.lightnovelreader.api.userdata.StringUserData
import java.text.DecimalFormat
import kotlin.math.roundToInt

/**
 * 基础的设置项控件
 *
 * 可基于该控件封装自定义设置项
 *
 * @param modifier Modifier 修饰符
 * @param painter 设置项图标
 * @param title 设置项标题
 * @param description 设置项描述文字
 * @param trailingContent 右侧内容区域
 * @param belowContent 标题与介绍下方的内容区域
 * @param extraBelowContent 所有控件下方的内容区域 (全宽度)
 * @param onClick 点击回调
 * @param onLongClick 长按回调
 * @param enabled 是否启用点击交互
 * @param interactionSource 外部交互源
 *
 * @since Api 4
 */
@Composable
fun SettingsBasicEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    description: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    belowContent: (@Composable ColumnScope.() -> Unit)? = null,
    extraBelowContent: (@Composable ColumnScope.() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val cornerRadius by animateDpAsState(
        targetValue = if (pressed) 16.dp else 4.dp,
        animationSpec = tween(durationMillis = 160),
        label = "settingsEntryCornerRadius"
    )

    val clickModifier =
        if (onClick != null || onLongClick != null) {
            Modifier.combinedClickable(
                interactionSource = source,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = {
                    onClick?.invoke()
                },
                onLongClick = onLongClick
            )
        } else Modifier

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(modifier)
            .fillMaxWidth()
            .then(clickModifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.55f)
                .padding(horizontal = 22.dp)
                .padding(vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                painter?.let {
                    Icon(
                        modifier = Modifier
                            .padding(end = 22.dp)
                            .size(24.dp),
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

                    belowContent?.invoke(this)
                }

                if (trailingContent != null) {
                    Box(
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        trailingContent()
                    }
                }
            }

            extraBelowContent?.invoke(this)
        }
    }
}

/**
 * 带开关的设置项控件
 * 通过 [BooleanUserData] 控制开关状态, 切换时会异步写入用户数据
 *
 * @param modifier Modifier修饰符
 * @param painter 图标
 * @param title 设置项标题
 * @param description 设置项描述文字
 * @param checked 当前开关状态
 * @param booleanUserData 关联的 [BooleanUserData] 用户数据对象
 * @param enabled 是否启用此设置项
 *
 * @since Api 2
 */
@Composable
fun SettingsSwitchEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    description: String,
    checked: Boolean,
    booleanUserData: BooleanUserData,
    enabled: Boolean = true
) {
    SettingsSwitchEntry(
        modifier = modifier,
        painter = painter,
        title = title,
        description = description,
        checked = checked,
        onCheckedChange = booleanUserData::asynchronousSet,
        enabled = enabled
    )
}

/**
 * 带开关的设置项控件
 *
 * @param modifier Modifier修饰符
 * @param painter 图标
 * @param title 设置项标题
 * @param description 设置项描述文字
 * @param checked 当前开关状态
 * @param onCheckedChange 开关状态改变时的回调
 * @param enabled 是否启用此设置项
 *
 * @since Api 2
 */
@Composable
fun SettingsSwitchEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hapticFeedback = LocalHapticFeedback.current
    val checkedChange: (Boolean) -> Unit = {
        hapticFeedback.performHapticFeedback(
            if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff
        )
        onCheckedChange(it)
    }

    SettingsBasicEntry(
        enabled = enabled,
        modifier = modifier.toggleable(
            value = checked,
            interactionSource = interactionSource,
            role = Role.Switch,
            enabled = enabled,
            indication = LocalIndication.current,
            onValueChange = checkedChange
        ),
        painter = painter,
        title = title,
        description = description,
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                interactionSource = interactionSource
            )
        },
        interactionSource = interactionSource
    )
}

/**
 * 可点击并跳转 URL 的设置项控件
 *
 * @param modifier Modifier修饰符
 * @param painter 图标
 * @param title 设置项标题
 * @param description 设置项描述文字
 * @param openUrl 点击后用系统浏览器打开的URL
 *
 * @since Api 2
 */
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

/**
 * 可点击并执行操作的设置项控件
 *
 * @param modifier Modifier修饰符
 * @param painter 图标
 * @param title 设置项标题
 * @param option 右侧显示的当前选项值文字(可选)
 * @param trailingContent 右侧自定义Composable内容(可选)
 * @param description 设置项描述文字(可选)
 * @param onClick 点击时的回调
 *
 * @since Api 2
 */
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
    val hapticFeedback = LocalHapticFeedback.current
    SettingsBasicEntry(
        modifier = modifier,
        painter = painter,
        title = title,
        description = description,
        belowContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
        },
        trailingContent = {
            trailingContent?.invoke()
        },
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
            onClick()
        }
    )
}

/**
 * 菜单选择设置项控件
 *
 * 通过 [StringUserData] 持久化数值, 切换时会异步写入用户数据
 *
 * @param modifier Modifier修饰符
 * @param painter 图标
 * @param title 设置项标题
 * @param description 设置项描述文字
 * @param options 选项容器
 * @param enabled 是否启用此设置项
 * @param selectedOptionKey 当前选定选项的键值
 * @param stringUserData 关联的 [StringUserData] 用户数据对象
 *
 * @since Api 4
 */
@Composable
fun SettingsMenuEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    description: String? = null,
    options: SettingsMenuOptionGroup,
    enabled: Boolean = true,
    selectedOptionKey: String,
    stringUserData: StringUserData
) {
    SettingsMenuEntry(
        modifier = modifier,
        painter = painter,
        title = title,
        description = description,
        options = options,
        enabled = enabled,
        selectedOptionKey = selectedOptionKey,
        onOptionChange = { stringUserData.asynchronousSet(it) }
    )
}

/**
 * 菜单选择设置项控件
 *
 * @param modifier Modifier修饰符
 * @param painter 图标
 * @param title 设置项标题
 * @param description 设置项描述文字
 * @param options 选项容器
 * @param enabled 是否启用此设置项
 * @param selectedOptionKey 当前选定选项的键值
 * @param onOptionChange 选定选项变化时的回调
 *
 * @since Api 4
 */
@Composable
fun SettingsMenuEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    description: String? = null,
    options: SettingsMenuOptionGroup,
    enabled: Boolean = true,
    selectedOptionKey: String,
    onOptionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember(options) {
        mutableStateOf(options.getOrNull(selectedOptionKey))
    }
    LaunchedEffect(options, selectedOptionKey) {
        selectedOption = options.getOrNull(selectedOptionKey)
    }
    val hapticFeedback = LocalHapticFeedback.current

    SettingsBasicEntry(
        modifier = modifier,
        painter = painter,
        title = title,
        description = description,
        enabled = enabled,
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
            expanded = !expanded
        },
        belowContent = {
            AnimatedTextLine(
                text = selectedOption?.let { stringResource(it.nameId) } ?: "(null)",
                style = typography.bodyMedium,
                color = colorScheme.primary
            )
            Box {
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
    )
}

/**
 * 滑块数值设置项控件
 * 通过 [FloatUserData] 持久化数值, 切换时会异步写入用户数据
 *
 *
 * @param modifier Modifier 修饰符
 * @param painter 图标
 * @param title 设置项标题
 * @param unit 数值单位文字
 * @param value 当前数值
 * @param enabled 是否启用交互 (含长按编辑)
 * @param valueRange 数值范围
 * @param valueFormat 连续取值时的格式化函数
 * @param floatUserData 关联的 [FloatUserData] 用户数据对象
 * @param steps 离散步进值列表
 *
 * @since Api 4
 */
@Composable
fun SettingsSliderEntry(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    title: String,
    unit: String,
    value: Float,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    valueFormat: (Float) -> Float = { (it * 2).roundToInt().toFloat() / 2 },
    decimalFormat: String = "0.0",
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
        enabled = enabled,
        valueRange = valueRange,
        valueFormat = valueFormat,
        decimalFormat = decimalFormat,
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
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    valueFormat: (Float) -> Float,
    decimalFormat: String,
    steps: List<Float>? = null,
    onSlideChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val actualSteps = steps?.distinct()?.sorted()
    val sliderValue = if (actualSteps != null) {
        actualSteps.indexOfFirst { it == value }.takeIf { it >= 0 }?.toFloat()
            ?: actualSteps.indexOfFirst { it > value }.coerceAtLeast(0).toFloat()
    } else value

    val animatedPosition by animateFloatAsState(
        targetValue = sliderValue,
        animationSpec = tween(
            durationMillis = 80,
            easing = LinearEasing
        )
    )

    val sliderRange = if (actualSteps != null) 0f..(actualSteps.lastIndex.toFloat())
    else valueRange

    val stepsCount = if (actualSteps != null) actualSteps.size - 2 else 0

    val hapticFeedback = LocalHapticFeedback.current
    var lastHapticValue by remember(value) { mutableFloatStateOf(value) }

    SettingsBasicEntry(
        modifier = modifier,
        painter = painter,
        title = title,
        extraBelowContent = {
            Slider(
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                value = animatedPosition,
                valueRange = sliderRange,
                steps = stepsCount,
                onValueChange = { raw ->
                    val newValue = if (actualSteps != null) {
                        val index = raw.roundToInt().coerceIn(0, actualSteps.lastIndex)
                        actualSteps[index]
                    } else {
                        valueFormat(raw)
                    }
                    if (newValue != lastHapticValue) {
                        lastHapticValue = newValue
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                    }
                    onSlideChange(newValue)
                },
                onValueChangeFinished = onSliderChangeFinished,
                colors = SliderDefaults.colors(
                    inactiveTrackColor = colorScheme.primaryContainer,
                ),
            )
        },
        trailingContent = {
            Row {
                AnimatedText(
                    text = DecimalFormat(decimalFormat).format(
                        if (actualSteps != null) {
                            actualSteps[sliderValue.toInt()]
                        } else {
                            value
                        }
                    ),
                    color = colorScheme.primary,
                    style = typography.bodyMedium,
                    maxLines = 1
                )

                Spacer(Modifier.width(2.dp))

                Text(
                    text = unit,
                    color = colorScheme.primary,
                    style = typography.bodyMedium,
                    maxLines = 1
                )
            }
        },
        onLongClick = onLongClick
    )
}

private fun NavController.navigateToSliderValueDialog(path: String, value: Float) {
    navigate(Route.SliderValueDialog(value, path))
}
