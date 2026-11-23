package io.nightfish.lightnovelreader.api.content.component.data

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import io.nightfish.lightnovelreader.api.content.component.ComponentDataJsonElementSerializer
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.content.component.data.cap.Divisible
import io.nightfish.lightnovelreader.api.content.component.data.cap.TextData
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.identifier.ofAppId
import io.nightfish.lightnovelreader.api.text.ParagraphNode
import io.nightfish.lightnovelreader.api.text.TextNode
import io.nightfish.lightnovelreader.api.text.layout.resolveVerticalTypefaces
import io.nightfish.lightnovelreader.api.text.layout.verticalParagraphText
import io.nightfish.lightnovelreader.api.ui.ReaderStyle
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.dom4j.DocumentHelper
import org.dom4j.Element

/**
 * 段落组件数据
 * 支持文本后处理与根据可用空间进行段落切分
 *
 * @param paragraph 段落的文本节点与样式
 * @param index 切分后的片段序号, 从1开始
 * @param split 是否为切分后的段落片段
 *
 * @since Api 4
 */
@Serializable
data class ParagraphComponentData(
    val paragraph: ParagraphNode,
    @Transient
    override val index: Int = 1,
    @Transient
    override val split: Boolean = false
): AbstractContentComponentData(),
    TextData<ParagraphComponentData>,
    Divisible<ParagraphComponentData>
{
    override val id: Identifier = Companion.id

    /**
     * 合并阅读器、段落与文本节点样式, 生成用于测绘和渲染的文本
     *
     * @param readerStyle 阅读器样式设置
     * @param baseStyle 基底文本样式
     * @param noIndent 是否取消首行的额外缩进, 为true时使用其余行的缩进
     * @param isDarkMode 是否使用阅读器的深色模式文本颜色
     *
     * @return 保留段落样式与文本样式的富文本
     *
     * @since Api 4
     */
    fun toAnnotatedString(
        readerStyle: ReaderStyle,
        baseStyle: TextStyle,
        noIndent: Boolean = false,
        isDarkMode: Boolean = false
    ) = run {
        val paragraphStyle = baseStyle
            .toParagraphStyle()
            .merge(readerStyle.toParagraphStyle())
            .merge(paragraph.style.toParagraphStyle())
            .merge(ParagraphStyle(textAlign = paragraph.style.textStyle?.textAlign ?: TextAlign.Unspecified))
            .let {
                if (!noIndent) return@let it
                it.merge(
                    ParagraphStyle(
                        textIndent = TextIndent(
                            firstLine = it.textIndent?.restLine ?: 0.sp,
                            restLine = it.textIndent?.restLine ?: 0.sp
                        )
                    )
                )
            }

        val spanStyle = baseStyle
            .toSpanStyle()
            .merge(readerStyle.toSpanStyle(darkMode = isDarkMode))
            .merge(paragraph.style.textStyle?.toSpanStyle())
        buildAnnotatedString {
            withStyle(paragraphStyle) {
                paragraph.textNodes.forEach { textNode ->
                    withStyle(spanStyle.merge(textNode.style.toSpanStyle())) {
                        append(textNode.text)
                    }
                }
            }
        }
    }

    override fun toJsonElement(): JsonElement = jsonSerializer.toJsonElement(this)

    override fun toHtmlElement(context: Context): Element {
        return DocumentHelper.createElement("p").apply {
            paragraph.textNodes.forEach { textNode ->
                addElement("span").addText(textNode.text)
            }
        }
    }

    override suspend fun split(
        height: Int,
        width: Int,
        context: Context,
        readerStyle: ReaderStyle,
        baseStyle: TextStyle
    ): List<ParagraphComponentData> {
        if (height <= 0 || width <= 0 || paragraph.textNodes.isEmpty()) return listOf(this)

        val configuration = context.resources.configuration
        val density = Density(
            configuration.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT,
            configuration.fontScale,
        )
        val resolver = createFontFamilyResolver(context)
        val text = toAnnotatedString(readerStyle, baseStyle, index != 1)
        if (text.isEmpty()) return listOf(this)
        val spacing = with(density) {
            readerStyle.spacingBeforeParagraph.toDp().roundToPx() +
                readerStyle.spacingAfterParagraph.toDp().roundToPx()
        }

        val offsets = when (readerStyle.writeMode) {
            WriteMode.Horizontal -> {
                val direction = if (configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                    LayoutDirection.Rtl
                } else LayoutDirection.Ltr
                val layout = TextMeasurer(resolver, density, direction).measure(
                    text = text,
                    style = baseStyle,
                    constraints = Constraints(maxWidth = width),
                )
                if (layout.size.height + spacing <= height) return listOf(this)
                val lastLine = (0 until layout.lineCount).takeWhile {
                    layout.getLineBottom(it) <= height - spacing
                }.lastOrNull() ?: return listOf(this)
                val end = layout.getLineEnd(lastLine, visibleEnd = false)
                val trimmedEnd = if (end > 0 && text.text[end - 1] == '\n') {
                    if (end > 1 && text.text[end - 2] == '\r') end - 2 else end - 1
                } else end
                trimmedEnd to end
            }
            WriteMode.VerticalLTR, WriteMode.VerticalRTL -> {
                val typefaces = resolveVerticalTypefaces(text, baseStyle, resolver)
                verticalParagraphText(text, baseStyle, density, typefaces)
                    .layout(height)
                    .splitOffsets(width - spacing)
            }
        } ?: return listOf(this)
        val (headEnd, tailStart) = offsets
        if (tailStart <= 0 || headEnd >= text.length) return listOf(this)

        fun fragment(start: Int, end: Int, partIndex: Int) = copy(
            paragraph = paragraph.copy(textNodes = paragraph.textNodes.slice(start, end)),
            index = partIndex,
            split = true,
        )

        return listOf(fragment(0, headEnd, index), fragment(tailStart, text.length, index + 1))
    }

    override fun processText(processor: (text: String) -> String) =
        ParagraphComponentData(
            paragraph.copy(
                textNodes = paragraph.textNodes.map { textNode ->
                    textNode.copy(
                        text = processor.invoke(textNode.text)
                    )
                }
            )
        )

    /**
     * [ParagraphComponentData]工厂方法和常量集合
     *
     * @since Api 4
     */
    companion object {
        /** 文本件的唯一标识字符串 */
        val id = "paragraph".ofAppId()

        /** 默认JSON序列化器 */
        val jsonSerializer = object : ComponentDataJsonElementSerializer<ParagraphComponentData> {
            override fun toJsonElement(data: ParagraphComponentData): JsonElement =
                Json.encodeToJsonElement(data)

            override fun fromJsonElement(json: JsonElement): ParagraphComponentData =
                Json.decodeFromJsonElement(json)
        }
    }
}

private fun Collection<TextNode>.slice(
    start: Int,
    end: Int,
): List<TextNode> = buildList {
    var nodeStart = 0
    this@slice.forEach { node ->
        val nodeEnd = nodeStart + node.text.length
        val sliceStart = maxOf(start, nodeStart)
        val sliceEnd = minOf(end, nodeEnd)
        if (sliceStart < sliceEnd) {
            add(
                node.copy(
                    text = node.text.substring(sliceStart - nodeStart, sliceEnd - nodeStart)
                )
            )
        }
        nodeStart = nodeEnd
    }
}
