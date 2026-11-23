package io.nightfish.lightnovelreader.api.text

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.nightfish.lightnovelreader.api.serializer.ColorSerializer
import io.nightfish.lightnovelreader.api.serializer.FontWeightSerializer
import io.nightfish.lightnovelreader.api.serializer.TextAlignSerializer
import io.nightfish.lightnovelreader.api.serializer.TextUnitSerializer
import io.nightfish.lightnovelreader.api.text.font.FontConfig
import io.nightfish.lightnovelreader.api.text.font.FontVariationConfig
import io.nightfish.lightnovelreader.api.text.font.VariationItem
import kotlinx.serialization.Serializable
import java.io.File

/**
 * 文本样式配置
 * 未指定的属性由段落样式或阅读器样式决定
 *
 * @param fontSize 字体大小
 * @param fontFamily 本地字体配置集合, 按顺序组成字体族
 * @param fontWeight 字体粗细, null表示未指定
 * @param color 文本颜色
 * @param underline 是否绘制下划线, null表示未指定
 * @param throughline 是否绘制删除线, null表示未指定
 * @param italic 是否使用斜体, null表示未指定
 * @param letterSpacing 字间距
 * @param textAlign 文本对齐方式
 *
 * @since Api 4
 */
@Stable
@Serializable
data class TextStyle(
    @Serializable(TextUnitSerializer::class)
    val fontSize: TextUnit = TextUnit.Unspecified,
    val fontFamily: Collection<FontConfig> = emptyList(),
    @Serializable(FontWeightSerializer::class)
    val fontWeight: FontWeight? = null,
    @Serializable(ColorSerializer::class)
    val color: Color = Color.Unspecified,
    val underline: Boolean? = null,
    val throughline: Boolean? = null,
    val italic: Boolean? = null,
    @Serializable(TextUnitSerializer::class)
    val letterSpacing: TextUnit = TextUnit.Unspecified,
    @Serializable(TextAlignSerializer::class)
    val textAlign: TextAlign = TextAlign.Unspecified
) {
    /**
     * 转换为Compose文本样式
     * 无法读取的本地字体会被忽略, 文本对齐方式由段落样式处理
     *
     * @return 包含字体、颜色与文本修饰的Compose文本样式
     *
     * @since Api 4
     */
    fun toSpanStyle() = SpanStyle(
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontStyle = italic?.let {
            if (it) FontStyle.Italic
            else FontStyle.Normal
        },
        fontFamily = fontFamily
            .takeIf { it.isNotEmpty() }
            ?.mapNotNull(FontConfig::toComposeFont)
            ?.takeIf { it.isNotEmpty() }
            ?.let { FontFamily(*it.toTypedArray()) },
        letterSpacing = letterSpacing,
        textDecoration = toTextDecoration(),
    )

    private fun toTextDecoration(): TextDecoration? {
        if (underline == null && throughline == null) return null

        val decorations = buildList {
            if (underline == true) add(TextDecoration.Underline)
            if (throughline == true) add(TextDecoration.LineThrough)
        }
        return if (decorations.isEmpty()) {
            TextDecoration.None
        } else {
            TextDecoration.combine(decorations)
        }
    }
}

private fun FontConfig.toComposeFont() = runCatching {
    val fontFile = uri
        .takeUnless { it == Uri.EMPTY }
        ?.path
        ?.takeIf(String::isNotBlank)
        ?.let(::File)
        ?.takeIf(File::isFile)
        ?: return@runCatching null

    Font(
        file = fontFile,
        weight = weight,
        style = style,
        variationSettings = variationSettings.toComposeSettings(weight, style),
    )
}.getOrNull()

private fun FontVariationConfig?.toComposeSettings(
    weight: FontWeight,
    style: FontStyle,
): FontVariation.Settings {
    val configuredSettings = this
        ?.settings
        ?.mapNotNull(VariationItem::toComposeSetting)
        ?.distinctBy { it.axisName }
        .orEmpty()
    val configuredAxes = configuredSettings.mapTo(mutableSetOf()) { it.axisName }

    val settings = buildList {
        if ("wght" !in configuredAxes) add(FontVariation.weight(weight.weight))
        if ("ital" !in configuredAxes) add(FontVariation.italic(style.value.toFloat()))
        addAll(configuredSettings)
    }
    return FontVariation.Settings(*settings.toTypedArray())
}

private fun VariationItem.toComposeSetting(): FontVariation.Setting? = runCatching {
    when (this) {
        is VariationItem.FloatValue -> FontVariation.Setting(axis, value)
        is VariationItem.IntValue -> FontVariation.Setting(axis, value.toFloat())
        is VariationItem.TextUnitValue -> {
            val serializedValue = value.trim()
            val numericValue = serializedValue
                .removeSuffix("sp")
                .removeSuffix("em")
                .toFloatOrNull()
                ?: return@runCatching null

            if (axis == "opsz" && serializedValue.endsWith("sp")) {
                FontVariation.opticalSizing(numericValue.sp)
            } else {
                FontVariation.Setting(axis, numericValue)
            }
        }
    }
}.getOrNull()
