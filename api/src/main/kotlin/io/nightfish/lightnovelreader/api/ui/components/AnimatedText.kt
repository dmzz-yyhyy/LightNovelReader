package io.nightfish.lightnovelreader.api.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel

private const val WheelQueueCapacity = 5
private const val WheelDurationMillis = 170

@Composable
private fun <T : Any> Wheel(
    target: T,
    modifier: Modifier = Modifier,
    content: @Composable (
        value: T,
        modifier: Modifier
    ) -> Unit
) {
    val queue = remember {
        Channel<T>(
            capacity = WheelQueueCapacity,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }

    val lastSubmitted = remember { object { var value = target } }
    var current by remember { mutableStateOf(target) }
    var next by remember { mutableStateOf<T?>(null) }
    var height by remember { mutableIntStateOf(0) }
    val progress = remember { Animatable(0f) }

    SideEffect {
        if (lastSubmitted.value != target) {
            lastSubmitted.value = target
            queue.trySend(target)
        }
    }

    LaunchedEffect(queue) {
        while (true) {
            var targetValue = queue.receiveCatching().getOrNull() ?: break

            while (true) {
                queue.tryReceive().getOrNull()?.let { targetValue = it } ?: break
            }

            if (targetValue == current) continue

            next = targetValue
            progress.snapTo(0f)

            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = WheelDurationMillis,
                    easing = FastOutSlowInEasing
                )
            )

            current = targetValue
            next = null
            progress.snapTo(0f)
        }
    }

    DisposableEffect(queue) {
        onDispose { queue.close() }
    }

    Box(
        modifier = modifier.clipToBounds()
            .onSizeChanged {
                height = it.height
            }
    ) {
        content(
            current,
            Modifier.graphicsLayer {
                translationY = (-height * progress.value)
            }
        )

        next?.let { nextValue ->
            content(
                nextValue,
                Modifier.graphicsLayer {
                    translationY = height * (1f - progress.value)
                }
            )
        }
    }
}

/**
 * 动画文本控件
 * 在文本变化时，提供逐字符滑动动画效果
 *
 * @param text 显示的文本内容
 * @param modifier Modifier修饰符
 * @param color 文本颜色
 * @param fontSize 字体大小
 * @param fontStyle 字体样式
 * @param fontWeight 字体粗细
 * @param fontFamily 字体族
 * @param letterSpacing 字间距
 * @param textDecoration 文本装饰
 * @param textAlign 文本对齐方式
 * @param lineHeight 行高
 * @param overflow 文本溢出处理方式
 * @param softWrap 是否软换行
 * @param maxLines 最大行数
 * @param minLines 最小行数
 * @param onTextLayout 文本布局完成回调
 * @param style 文本样式
 *
 * @since Api 2
 */
@Composable
fun AnimatedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in text.indices) {
            key(i) {
                Wheel(
                    target = text[i]
                ) { char, wheelModifier ->
                    Text(
                        text = char.toString(),
                        modifier = wheelModifier,
                        style = style,
                        color = color,
                        softWrap = softWrap,
                        fontSize = fontSize,
                        fontStyle = fontStyle,
                        fontWeight = fontWeight,
                        fontFamily = fontFamily,
                        letterSpacing = letterSpacing,
                        textDecoration = textDecoration,
                        textAlign = textAlign,
                        lineHeight = lineHeight,
                        overflow = overflow,
                        maxLines = maxLines,
                        minLines = minLines,
                        onTextLayout = onTextLayout
                    )
                }
            }
        }
    }
}

/**
 * 动画文本控件
 *
 * 区别于 AnimatedText, 该控件在文本变化时，提供整行的滑动动画效果
 */
@Composable
fun AnimatedTextLine(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Wheel(
            target = text
        ) { value, wheelModifier ->
            Text(
                text = value,
                modifier = wheelModifier,
                style = style,
                color = color,
                softWrap = softWrap,
                fontSize = fontSize,
                fontStyle = fontStyle,
                fontWeight = fontWeight,
                fontFamily = fontFamily,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                maxLines = maxLines,
                minLines = minLines,
                onTextLayout = onTextLayout
            )
        }
    }
}