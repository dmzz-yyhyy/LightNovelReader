@file:Suppress("unused")

package io.nightfish.lightnovelreader.api.content.builder

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import io.nightfish.lightnovelreader.api.content.component.data.ParagraphComponentData
import io.nightfish.lightnovelreader.api.text.ParagraphNode
import io.nightfish.lightnovelreader.api.text.ParagraphStyle
import io.nightfish.lightnovelreader.api.text.TextNode
import io.nightfish.lightnovelreader.api.text.TextStyle
import io.nightfish.lightnovelreader.api.text.font.FontConfig
import io.nightfish.lightnovelreader.api.text.font.FontVariationConfig
import io.nightfish.lightnovelreader.api.text.font.VariationItem

fun ContentBuilder.paragraph(
    builder: ParagraphBuilder.() -> Unit
): ContentBuilder =
    this.component(
        ParagraphBuilder()
            .apply(builder)
            .build()
    )

class ParagraphBuilder {
    private val textNodes = mutableListOf<TextNode>()
    private var style: ParagraphStyle = ParagraphStyle()

    fun text(text: String, textStyleBuilder: TextStyleBuilder.() -> Unit) {
        textNodes.add(
            TextNode(
                text,
                TextStyleBuilder()
                    .apply(textStyleBuilder)
                    .build()
            )
        )
    }

    fun text(text: String) {
        textNodes.add(TextNode(text))
    }

    fun style(builder: ParagraphStyleBuilder.() -> Unit) {
        this.style = ParagraphStyleBuilder()
            .apply(builder)
            .build()
    }

    fun build() = ParagraphComponentData(
        ParagraphNode(
        textNodes.toList(),
        style
        )
    )
}

class ParagraphStyleBuilder {
    private var textIndent: TextIndent? = null
    private var spacingBefore: TextUnit = TextUnit.Unspecified
    private var spacingAfter: TextUnit = TextUnit.Unspecified
    private var fontSize: TextUnit = TextUnit.Unspecified
    private var fontFamily: Collection<FontConfig> = emptyList()
    private var fontWeight: FontWeight? = null
    private var color: Color = Color.Unspecified
    private var underline: Boolean? = null
    private var throughline: Boolean? = null
    private var italic: Boolean? = null
    private var letterSpacing: TextUnit = TextUnit.Unspecified
    private var lineHeight: TextUnit = TextUnit.Unspecified
    private var textAlign: TextAlign = TextAlign.Unspecified

    fun fontSize(textUnit: TextUnit) {
        this.fontSize = textUnit
    }

    fun fontFamily(builder: FontFamilyBuilder.() -> Unit) {
        this.fontFamily = FontFamilyBuilder()
            .apply(builder)
            .build()
    }

    fun fontWeight(fontWeight: FontWeight) {
        this.fontWeight = fontWeight
    }

    fun color(color: Color) {
        this.color = color
    }

    fun underline(underline: Boolean) {
        this.underline = underline
    }

    fun italic(italic: Boolean) {
        this.italic = italic
    }

    fun throughline(throughline: Boolean) {
        this.throughline = throughline
    }

    fun letterSpacing(textUnit: TextUnit) {
        this.letterSpacing = textUnit
    }

    fun lineHeight(textUnit: TextUnit) {
        this.lineHeight = textUnit
    }

    fun textAlign(textAlign: TextAlign) {
        this.textAlign = textAlign
    }

    fun textIndent(textIndent: TextIndent) {
        this.textIndent = textIndent
    }

    fun spacingBefore(textUnit: TextUnit) {
        this.spacingBefore = textUnit
    }

    fun spacingAfter(textUnit: TextUnit) {
        this.spacingAfter = textUnit
    }


    fun build(): ParagraphStyle = ParagraphStyle(
        lineHeight,
        textIndent,
        spacingBefore,
        spacingAfter,
        TextStyle(
            fontSize,
            fontFamily,
            fontWeight,
            color,
            underline,
            throughline,
            italic,
            letterSpacing,
            textAlign
        )
    )
}

class TextStyleBuilder {
    private var fontSize: TextUnit = TextUnit.Unspecified
    private var fontFamily: Collection<FontConfig> = emptyList()
    private var fontWeight: FontWeight? = null
    private var color: Color = Color.Unspecified
    private var underline: Boolean? = null
    private var throughline: Boolean? = null
    private var italic: Boolean? = null
    private var letterSpacing: TextUnit = TextUnit.Unspecified
    private var textAlign: TextAlign = TextAlign.Unspecified

    fun fontSize(textUnit: TextUnit) {
        this.fontSize = textUnit
    }

    fun fontFamily(builder: FontFamilyBuilder.() -> Unit) {
        this.fontFamily = FontFamilyBuilder()
            .apply(builder)
            .build()
    }

    fun fontWeight(fontWeight: FontWeight) {
        this.fontWeight = fontWeight
    }

    fun color(color: Color) {
        this.color = color
    }

    fun underline(underline: Boolean) {
        this.underline = underline
    }

    fun throughline(throughline: Boolean) {
        this.throughline = throughline
    }

    fun italic(italic: Boolean) {
        this.italic = italic
    }

    fun letterSpacing(textUnit: TextUnit) {
        this.letterSpacing = textUnit
    }

    fun textAlign(textAlign: TextAlign) {
        this.textAlign = textAlign
    }

    fun build(): TextStyle = TextStyle(
        fontSize,
        fontFamily,
        fontWeight,
        color,
        underline,
        throughline,
        italic,
        letterSpacing,
        textAlign
    )
}

class FontFamilyBuilder {

    class FontConfigBuilder {

        class FontVariationConfigBuilder {
            private val settings = mutableListOf<VariationItem>()

            fun setting(axis: String, value: Float) {
                add(VariationItem.FloatValue(axis, value))
            }

            fun setting(axis: String, value: Int) {
                add(VariationItem.IntValue(axis, value))
            }

            fun setting(axis: String, value: TextUnit) {
                val serializedValue = when (value.type) {
                    TextUnitType.Sp -> "${value.value}sp"
                    TextUnitType.Em -> "${value.value}em"
                    else -> throw IllegalArgumentException(
                        "Font variation TextUnit must be specified as sp or em"
                    )
                }
                add(VariationItem.TextUnitValue(axis, serializedValue))
            }

            fun setting(item: VariationItem) {
                add(item)
            }

            private fun add(item: VariationItem) {
                require(item.axis.length == 4) {
                    "Font variation axis must contain exactly 4 characters: ${item.axis}"
                }
                require(settings.none { it.axis == item.axis }) {
                    "Font variation axis must be unique: ${item.axis}"
                }
                settings += item
            }

            fun build(): FontVariationConfig = FontVariationConfig(settings.toList())
        }

        fun build(): FontConfig = FontConfig(
            uri ?: throw Error("uri have to be not null"),
            weight,
            style,
            variationSettings
        )

        private var uri: Uri? = null
        private var weight: FontWeight = FontWeight.Normal
        private var style: FontStyle = FontStyle.Normal
        private var variationSettings: FontVariationConfig? = null

        fun uri(uri: Uri) {
            this.uri = uri
        }

        fun weight(weight: FontWeight) {
            this.weight = weight
        }

        fun style(style: FontStyle) {
            this.style = style
        }

        fun variationSettings(builder: FontVariationConfigBuilder.() -> Unit) {
            variationSettings = FontVariationConfigBuilder()
                .apply(builder)
                .build()
        }
    }

    private val fontConfigs = mutableListOf<FontConfig>()
    fun font(builder: FontConfigBuilder.() -> Unit) {
        fontConfigs.add(
            FontConfigBuilder()
                .apply(builder)
                .build()
        )
    }

    fun build() = fontConfigs.toList()
}
