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

/**
 * 向内容构建器添加一个段落组件
 *
 * @param builder 段落文本与样式的构建逻辑
 *
 * @return 当前内容构建器实例, 支持链式调用
 *
 * @since Api 4
 */
fun ContentBuilder.paragraph(
    builder: ParagraphBuilder.() -> Unit
): ContentBuilder =
    this.component(
        ParagraphBuilder()
            .apply(builder)
            .build()
    )

/**
 * 段落组件构建器
 *
 * @since Api 4
 */
class ParagraphBuilder {
    private val textNodes = mutableListOf<TextNode>()
    private var style: ParagraphStyle = ParagraphStyle()

    /**
     * 添加具有局部样式的文本节点
     *
     * @param text 文本内容
     * @param textStyleBuilder 当前文本节点的样式构建逻辑
     *
     * @since Api 4
     */
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

    /**
     * 使用默认文本样式添加文本节点
     *
     * @param text 文本内容
     *
     * @since Api 4
     */
    fun text(text: String) {
        textNodes.add(TextNode(text))
    }

    /**
     * 设置段落样式
     *
     * @param builder 段落样式的构建逻辑
     *
     * @since Api 4
     */
    fun style(builder: ParagraphStyleBuilder.() -> Unit) {
        this.style = ParagraphStyleBuilder()
            .apply(builder)
            .build()
    }

    /**
     * 构建段落组件数据
     *
     * @return 包含当前文本节点与样式的段落组件数据
     *
     * @since Api 4
     */
    fun build() = ParagraphComponentData(
        ParagraphNode(
        textNodes.toList(),
        style
        )
    )
}

/**
 * 段落样式构建器
 *
 * @since Api 4
 */
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

    /**
     * 设置字体大小
     *
     * @param textUnit 字体大小
     *
     * @since Api 4
     */
    fun fontSize(textUnit: TextUnit) {
        this.fontSize = textUnit
    }

    /**
     * 设置本地字体族配置
     *
     * @param builder 字体配置集合的构建逻辑
     *
     * @since Api 4
     */
    fun fontFamily(builder: FontFamilyBuilder.() -> Unit) {
        this.fontFamily = FontFamilyBuilder()
            .apply(builder)
            .build()
    }

    /**
     * 设置字体粗细
     *
     * @param fontWeight 字体粗细
     *
     * @since Api 4
     */
    fun fontWeight(fontWeight: FontWeight) {
        this.fontWeight = fontWeight
    }

    /**
     * 设置文本颜色
     *
     * @param color 文本颜色
     *
     * @since Api 4
     */
    fun color(color: Color) {
        this.color = color
    }

    /**
     * 设置绘制下划线
     *
     * @param underline 是否绘制下划线
     *
     * @since Api 4
     */
    fun underline(underline: Boolean) {
        this.underline = underline
    }

    /**
     * 设置使用斜体
     *
     * @param italic 是否使用斜体
     *
     * @since Api 4
     */
    fun italic(italic: Boolean) {
        this.italic = italic
    }

    /**
     * 设置绘制删除线
     *
     * @param throughline 是否绘制删除线
     *
     * @since Api 4
     */
    fun throughline(throughline: Boolean) {
        this.throughline = throughline
    }

    /**
     * 设置字间距
     *
     * @param textUnit 字间距
     *
     * @since Api 4
     */
    fun letterSpacing(textUnit: TextUnit) {
        this.letterSpacing = textUnit
    }

    /**
     * 设置行高
     *
     * @param textUnit 行高, 竖排时用于确定列宽
     *
     * @since Api 4
     */
    fun lineHeight(textUnit: TextUnit) {
        this.lineHeight = textUnit
    }

    /**
     * 设置文本对齐方式
     *
     * @param textAlign 文本对齐方式
     *
     * @since Api 4
     */
    fun textAlign(textAlign: TextAlign) {
        this.textAlign = textAlign
    }

    /**
     * 设置首行与其余行的缩进
     *
     * @param textIndent 首行与其余行的缩进
     *
     * @since Api 4
     */
    fun textIndent(textIndent: TextIndent) {
        this.textIndent = textIndent
    }

    /**
     * 设置段前间距
     *
     * @param textUnit 段前间距
     *
     * @since Api 4
     */
    fun spacingBefore(textUnit: TextUnit) {
        this.spacingBefore = textUnit
    }

    /**
     * 设置段后间距
     *
     * @param textUnit 段后间距
     *
     * @since Api 4
     */
    fun spacingAfter(textUnit: TextUnit) {
        this.spacingAfter = textUnit
    }


    /**
     * 构建段落样式
     *
     * @return 包含段落配置与基础文本样式的段落样式
     *
     * @since Api 4
     */
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

/**
 * 文本样式构建器
 *
 * @since Api 4
 */
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

    /**
     * 设置字体大小
     *
     * @param textUnit 字体大小
     *
     * @since Api 4
     */
    fun fontSize(textUnit: TextUnit) {
        this.fontSize = textUnit
    }

    /**
     * 设置本地字体族配置
     *
     * @param builder 字体配置集合的构建逻辑
     *
     * @since Api 4
     */
    fun fontFamily(builder: FontFamilyBuilder.() -> Unit) {
        this.fontFamily = FontFamilyBuilder()
            .apply(builder)
            .build()
    }

    /**
     * 设置字体粗细
     *
     * @param fontWeight 字体粗细
     *
     * @since Api 4
     */
    fun fontWeight(fontWeight: FontWeight) {
        this.fontWeight = fontWeight
    }

    /**
     * 设置文本颜色
     *
     * @param color 文本颜色
     *
     * @since Api 4
     */
    fun color(color: Color) {
        this.color = color
    }

    /**
     * 设置绘制下划线
     *
     * @param underline 是否绘制下划线
     *
     * @since Api 4
     */
    fun underline(underline: Boolean) {
        this.underline = underline
    }

    /**
     * 设置绘制删除线
     *
     * @param throughline 是否绘制删除线
     *
     * @since Api 4
     */
    fun throughline(throughline: Boolean) {
        this.throughline = throughline
    }

    /**
     * 设置使用斜体
     *
     * @param italic 是否使用斜体
     *
     * @since Api 4
     */
    fun italic(italic: Boolean) {
        this.italic = italic
    }

    /**
     * 设置字间距
     *
     * @param textUnit 字间距
     *
     * @since Api 4
     */
    fun letterSpacing(textUnit: TextUnit) {
        this.letterSpacing = textUnit
    }

    /**
     * 设置文本对齐方式
     *
     * @param textAlign 文本对齐方式
     *
     * @since Api 4
     */
    fun textAlign(textAlign: TextAlign) {
        this.textAlign = textAlign
    }

    /**
     * 构建文本样式
     *
     * @return 当前配置的文本样式
     *
     * @since Api 4
     */
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

/**
 * 字体族构建器
 *
 * @since Api 4
 */
class FontFamilyBuilder {

    /**
     * 本地字体配置构建器
     *
     * @since Api 4
     */
    class FontConfigBuilder {

        /**
         * 可变字体轴配置构建器
         * 同一配置内的字体轴标记不可重复
         *
         * @since Api 4
         */
        class FontVariationConfigBuilder {
            private val settings = mutableListOf<VariationItem>()

            /**
             * 添加一个字体变体轴配置
             *
             * @param axis 长度为4的字体轴标记, 如wght或opsz
             * @param value 浮点数字体轴取值
             *
             * @throws IllegalArgumentException 字体轴标记长度不为4、重复添加同一字体轴
             *
             * @since Api 4
             */
            fun setting(axis: String, value: Float) {
                add(VariationItem.FloatValue(axis, value))
            }

            /**
             * 添加一个字体变体轴配置
             *
             * @param axis 长度为4的字体轴标记, 如wght或opsz
             * @param value 整数字体轴取值
             *
             * @throws IllegalArgumentException 字体轴标记长度不为4、重复添加同一字体轴
             *
             * @since Api 4
             */
            fun setting(axis: String, value: Int) {
                add(VariationItem.IntValue(axis, value))
            }

            /**
             * 添加一个字体变体轴配置
             *
             * @param axis 长度为4的字体轴标记, 如wght或opsz
             * @param value 带sp或em单位的字体轴取值
             *
             * @throws IllegalArgumentException 字体轴标记长度不为4、重复添加同一字体轴或未指定文本单位
             *
             * @since Api 4
             */
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

            /**
             * 添加一个已构建的字体变体轴配置
             *
             * @param item 待添加的字体轴配置
             *
             * @throws IllegalArgumentException 字体轴标记长度不为4或重复添加同一字体轴
             *
             * @since Api 4
             */
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

            /**
             * 构建可变字体配置
             *
             * @return 按添加顺序保存字体轴的配置
             *
             * @since Api 4
             */
            fun build(): FontVariationConfig = FontVariationConfig(settings.toList())
        }

        /**
         * 构建本地字体配置
         *
         * @return 当前配置的本地字体
         *
         * @throws Error 尚未通过[uri]指定字体文件
         *
         * @since Api 4
         */
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

        /**
         * 设置本地字体文件Uri
         *
         * @param uri 本地字体文件Uri
         *
         * @since Api 4
         */
        fun uri(uri: Uri) {
            this.uri = uri
        }

        /**
         * 设置字体粗细
         *
         * @param weight 字体粗细
         *
         * @since Api 4
         */
        fun weight(weight: FontWeight) {
            this.weight = weight
        }

        /**
         * 设置字体样式
         *
         * @param style 字体样式
         *
         * @since Api 4
         */
        fun style(style: FontStyle) {
            this.style = style
        }

        /**
         * 设置可变字体轴配置
         *
         * @param builder 字体轴配置的构建逻辑
         *
         * @since Api 4
         */
        fun variationSettings(builder: FontVariationConfigBuilder.() -> Unit) {
            variationSettings = FontVariationConfigBuilder()
                .apply(builder)
                .build()
        }
    }

    private val fontConfigs = mutableListOf<FontConfig>()
    /**
     * 向字体族中添加一个本地字体配置
     *
     * @param builder 字体文件与样式的构建逻辑
     *
     * @since Api 4
     */
    fun font(builder: FontConfigBuilder.() -> Unit) {
        fontConfigs.add(
            FontConfigBuilder()
                .apply(builder)
                .build()
        )
    }

    /**
     * 构建字体族配置集合
     *
     * @return 按添加顺序保存的本地字体配置列表
     *
     * @since Api 4
     */
    fun build() = fontConfigs.toList()
}
