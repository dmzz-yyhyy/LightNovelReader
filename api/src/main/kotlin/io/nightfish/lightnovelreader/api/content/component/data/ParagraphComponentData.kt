package io.nightfish.lightnovelreader.api.content.component.data

import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import io.nightfish.lightnovelreader.api.content.component.ComponentDataJsonElementSerializer
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.identifier.ofAppId
import io.nightfish.lightnovelreader.api.text.ParagraphNode
import io.nightfish.lightnovelreader.api.text.TextNode
import io.nightfish.lightnovelreader.api.ui.ReaderStyle
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.dom4j.Element

@Serializable
data class ParagraphComponentData(
    val paragraph: ParagraphNode,
    @Transient
    override val index: Int = 1,
    @Transient
    override val split: Boolean = false
): AbstractContentComponentData(), TextData<ParagraphComponentData>, Divisible<ParagraphComponentData> {
    override val id: Identifier = Companion.id

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
            .let {
                if (!noIndent) return@let it
                it.merge(
                    ParagraphStyle(
                        textIndent = TextIndent(
                            firstLine = 0.sp,
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
        TODO("Not yet implemented")
    }

    override suspend fun split(
        height: Int,
        width: Int,
        context: Context,
        readerStyle: ReaderStyle,
        baseStyle: TextStyle
    ): List<ParagraphComponentData> {
        val textMeasurer = TextMeasurer(
            createFontFamilyResolver(context),
            Density(
                context.resources.configuration.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT,
                context.resources.configuration.fontScale,
            ),
            if (context.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_LTR) {
                LayoutDirection.Ltr
            } else {
                LayoutDirection.Rtl
            }
        )

        if (height <= 0 || width <= 0 || paragraph.textNodes.isEmpty()) {
            return listOf(this)
        }

        val annotatedText = this.toAnnotatedString(
            readerStyle,
            baseStyle,
            this.index != 1
        )

        if (annotatedText.isEmpty()) return listOf(this)

        val layout = textMeasurer.measure(
            text = annotatedText,
            constraints = Constraints(maxWidth = width),
        )
        if (layout.size.height <= height) return listOf(this)

        var lastFittingLine = -1
        for (lineIndex in 0 until layout.lineCount) {
            if (layout.getLineBottom(lineIndex) > height) break
            lastFittingLine = lineIndex
        }

        val lineIndex = lastFittingLine.coerceAtLeast(0)
        var splitOffset = layout.getLineEnd(lineIndex, visibleEnd = false)
        if (splitOffset <= 0) {
            splitOffset = annotatedText.text.offsetByCodePoints(0, 1)
        }
        if (splitOffset >= annotatedText.length) return listOf(this)

        return listOf(0 until splitOffset, splitOffset until annotatedText.length)
            .mapIndexed { partIndex, range ->
                ParagraphComponentData(
                    paragraph = paragraph.copy(
                        textNodes = paragraph.textNodes.slice(range.first, range.last + 1)
                    ),
                    index = index + partIndex,
                    split = true,
                )
            }
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

    override val contentLengthWeight = paragraph.textNodes.sumOf { it.text.length }

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
