/*
 * This file incorporates code from FooIbar/EhViewer
 * Copyright 2014-2019 Hippo Seven
 * Copyright 2020-2022 NekoInverter
 * Copyright 2022-2023 Tarsin Norbin
 * Copyright 2023-2024 Foolbar
 *
 * This file is part of LightNovelReader, licensed under GPLv3.
 */

package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.animation.core.animateOffset
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.toSize
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin

@Composable
fun rememberTextStyleNumberMaxSize(textStyle: TextStyle): DpSize {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    return remember(textStyle, textMeasurer, density) {
        with(density) {
            "0123456789".maxOfWith({ (aw, _), (bw, _) -> aw - bw }) { char ->
                textMeasurer.measure("$char", textStyle).size
            }.toSize().toDpSize()
        }
    }
}

@Suppress("UNUSED")
@Composable
fun RollingNumberPlaceholder(number: Int, style: TextStyle = LocalTextStyle.current) {
    val styleNoSpacing = style.copy(letterSpacing = TextUnit.Unspecified)
    val size = rememberTextStyleNumberMaxSize(style)
    Row(horizontalArrangement = Arrangement.Center) {
        "$number".forEach { char ->
            Text(
                text = "$char",
                style = styleNoSpacing,
                textAlign = TextAlign.Center,
                modifier = Modifier.size(size),
            )
        }
    }
}

private const val RADIUS = 100f

private const val GAP = (2 * PI / 10).toFloat()
private const val ZERO_RADIAN = -(PI / 2 - GAP / 2).toFloat()

private const val NULL_NODE_DISTANCE = 1.528f

private fun conceptSpaceToIntermediate(value: Int?): Offset {
    if (value != null) {
        val theta = ZERO_RADIAN - value * GAP
        return Offset(cos(theta), sin(theta)) * RADIUS
    } else {
        return Offset(0f, -NULL_NODE_DISTANCE) * RADIUS
    }
}

private fun intermediateToNumberOffset(circleOffset: Offset): Float {
    val degree = atan2(circleOffset.y, circleOffset.x)
    val normalized = normalize(ZERO_RADIAN - degree)
    return if (normalized > 9 * GAP) {
        val reNormalized = normalized - 9 * GAP - GAP / 2
        if (reNormalized > 0) {
            reNormalized / GAP * 2 - 1
        } else {
            10 + reNormalized / GAP * 2
        }
    } else {
        normalized / GAP
    }
}

private fun normalize(degree: Float) = (degree - 2 * PI * floor(degree / (2 * PI))).toFloat()

private fun numberOffsetToUIOffset(heightPx: Int, number: Float) =
    IntOffset(0, -(heightPx * 2 * number).toInt())

@Composable
fun RollingNumber(
    number: Int,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    separator: Boolean = false,
    length: Int? = null,
) {
    val isNegative = number < 0
    val textColor = color.takeOrElse { style.color.takeOrElse { LocalContentColor.current } }
    val string = remember(number) { "${abs(number)}" }
    val transition = updateTransition(string)
    val styleNoSpacing = style.merge(color = textColor).copy(letterSpacing = TextUnit.Unspecified)
    val size = rememberTextStyleNumberMaxSize(style)
    LazyRow(
        modifier = modifier
            .clipToBounds()
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(maxHeight = Int.MAX_VALUE))
                layout(width = placeable.width, height = size.height.roundToPx()) {
                    placeable.place(0, 0)
                }
            },
        reverseLayout = true,
        horizontalArrangement = Arrangement.Center,
    ) {
        val max = with(transition) { max(currentState.length, targetState.length) }
        items(max, key = { it }) { reversed ->
            val rotate by transition.animateOffset { str ->
                val len = str.length
                val absent = max - len
                val where = max - reversed - 1
                val v = if (where < absent) null else str[where - absent].digitToInt()
                conceptSpaceToIntermediate(v)
            }
            if (reversed != 0 && reversed % 3 == 0 && separator) {
                Text(
                    text = ",",
                    modifier = Modifier.size(size),
                    style = styleNoSpacing,
                    textAlign = TextAlign.Center,
                )
            }
            Column(
                modifier = Modifier
                    .animateItem()
                    .offset {
                        val number = intermediateToNumberOffset(rotate)
                        numberOffsetToUIOffset(size.height.roundToPx(), number)
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                repeat(10) { i ->
                    Text(
                        text = "$i",
                        modifier = Modifier.size(size),
                        style = styleNoSpacing,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "",
                        modifier = Modifier.size(size),
                        style = styleNoSpacing,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        if (isNegative) {
            item(key = -1) {
                Text(
                    text = "-",
                    modifier = Modifier
                        .size(size)
                        .animateItem(),
                    style = styleNoSpacing,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (length != null) {
            items(length - max - if (isNegative) 1 else 0) {
                Spacer(Modifier.size(size))
            }
        }
    }
}