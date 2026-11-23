package io.nightfish.lightnovelreader.api.text.layout

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.icu.text.BreakIterator
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import androidx.annotation.RestrictTo
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.text.vertical.TextOrientation
import androidx.text.vertical.VerticalTextLayout
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import kotlin.math.ceil

/**
 * 解析竖排文本使用的字体
 *
 * @param text 包含局部文本样式的富文本
 * @param style 基底文本样式
 * @param resolver Compose字体解析器
 *
 * @return 首项为基底字体, 其余项按文本样式区间顺序排列的字体列表
 *
 * @since Api 4
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun resolveVerticalTypefaces(
    text: AnnotatedString,
    style: TextStyle,
    resolver: FontFamily.Resolver,
): List<Typeface> = (listOf(style.toSpanStyle()) + text.spanStyles.map { style.toSpanStyle().merge(it.item) })
    .map {
        resolver.resolve(
            it.fontFamily,
            it.fontWeight ?: FontWeight.Normal,
            it.fontStyle ?: FontStyle.Normal,
            it.fontSynthesis ?: FontSynthesis.All,
        ).value as Typeface
    }

/**
 * 创建用于测绘和绘制的竖排段落文本
 * 合并基底样式与局部样式, 保留段落缩进和行高
 *
 * @param text 包含段落与局部文本样式的富文本
 * @param style 基底文本样式
 * @param density 屏幕密度与字体缩放信息
 * @param typefaces 由[resolveVerticalTypefaces]按相同文本与基底样式解析的字体列表
 *
 * @return 支持按可用高度断列的竖排段落文本
 *
 * @since Api 4
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun verticalParagraphText(
    text: AnnotatedString,
    style: TextStyle,
    density: Density,
    typefaces: List<Typeface>,
): VerticalParagraphText {
    val fontSize = style.fontSize.toPixels(density, with(density) { 16.sp.toPx() })
    val paint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
        this.density = density.density
        textSize = fontSize
        applyVerticalStyle(style.toSpanStyle(), typefaces.first(), density, fontSize)
        text.spanStyles.firstOrNull()?.let {
            applyVerticalStyle(style.toSpanStyle().merge(it.item), typefaces[1], density, fontSize)
        }
    }
    val spanned = SpannableString(text.text)
    text.spanStyles.forEachIndexed { index, range ->
        if (range.start < range.end) {
            val spanStyle = style.toSpanStyle().merge(range.item)
            spanned.setSpan(
                object : MetricAffectingSpan() {
                    override fun updateMeasureState(textPaint: TextPaint) {
                        textPaint.applyVerticalStyle(spanStyle, typefaces[index + 1], density, fontSize)
                    }

                    override fun updateDrawState(textPaint: TextPaint) = updateMeasureState(textPaint)
                },
                range.start,
                range.end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }
    val paragraphStyle = style.toParagraphStyle().merge(text.paragraphStyles.firstOrNull()?.item)
    val indent = paragraphStyle.textIndent ?: TextIndent.None
    val paragraphFontSize = text.spanStyles.firstOrNull()?.item?.fontSize
        ?.toPixels(density, fontSize) ?: fontSize
    return VerticalParagraphText(
        text = spanned,
        paint = paint,
        density = density,
        lineHeight = paragraphStyle.lineHeight,
        firstIndent = indent.firstLine.toPixels(density, paragraphFontSize),
        restIndent = indent.restLine.toPixels(density, paragraphFontSize),
    )
}

private fun TextPaint.applyVerticalStyle(
    style: SpanStyle,
    resolvedTypeface: Typeface,
    density: Density,
    baseFontSize: Float,
) {
    typeface = resolvedTypeface
    textSize = style.fontSize.toPixels(density, baseFontSize)
    letterSpacing = if (textSize > 0f) style.letterSpacing.toPixels(density, textSize, 0f) / textSize else 0f
    if (style.color.isSpecified) color = style.color.toArgb()
    if (style.background.isSpecified) bgColor = style.background.toArgb()
    fontFeatureSettings = style.fontFeatureSettings
    isUnderlineText = style.textDecoration?.contains(TextDecoration.Underline) == true
    isStrikeThruText = style.textDecoration?.contains(TextDecoration.LineThrough) == true
}

private fun TextUnit.toPixels(density: Density, fontSize: Float, fallback: Float = fontSize): Float = when {
    isSp -> with(density) { toPx() }
    isEm -> value * fontSize
    else -> fallback
}

// beta03 exposes column boundaries as library-group APIs. They are needed to retain the
// library's line breaking while applying paragraph indents, line spacing and LTR columns.
/**
 * 已解析样式与字体的竖排段落文本
 * 通过[verticalParagraphText]创建, 供测绘、段落切分与渲染共用断列结果
 *
 * @since Api 4
 */
@SuppressLint("RestrictedApi")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class VerticalParagraphText internal constructor(
    private val text: Spanned,
    private val paint: TextPaint,
    private val density: Density,
    private val lineHeight: TextUnit,
    private val firstIndent: Float,
    private val restIndent: Float,
) {
    private var cachedHeight = -1
    private var cachedLayout: VerticalParagraphLayout? = null

    /**
     * 根据可用高度对段落断列
     * 保留显式换行与空行, 缓存最近一次高度对应的排版结果
     *
     * @param height 每列可用高度, 单位为px, 支持[Constraints.Infinity]
     *
     * @return 当前高度下的排版结果, 高度为0时返回空排版
     *
     * @since Api 4
     */
    fun layout(height: Int): VerticalParagraphLayout {
        if (height == 0) return VerticalParagraphLayout(emptyList())
        cachedLayout?.takeIf { cachedHeight == height }?.let { return it }
        val columns = mutableListOf<VerticalParagraphColumn>()
        val availableHeight = if (height == Constraints.Infinity) {
            Float.POSITIVE_INFINITY
        } else {
            height.toFloat()
        }
        var start = 0
        // Handle explicit empty lines here, since the library cannot break an empty run.
        text.toString().split('\n').forEach { line ->
            val end = start + line.removeSuffix("\r").length
            val nextStart = (start + line.length + 1).coerceAtMost(text.length)
            val indent = (if (start == 0) firstIndent else restIndent).coerceAtLeast(0f)
                .coerceAtMost(availableHeight)
            if (start == end) {
                columns.add(VerticalParagraphColumn(null, columnWidth(paint.textSize), indent, 0f, end, nextStart))
            } else {
                val first = VerticalTextLayout(
                    text = text,
                    start = start,
                    end = end,
                    paint = paint,
                    height = (availableHeight - indent).coerceAtLeast(0f),
                )
                val firstEnd = first.getLineEnd(0)
                columns.add(column(start, firstEnd, indent, if (firstEnd == end) nextStart else firstEnd))
                if (firstEnd < end) {
                    val remainingIndent = restIndent.coerceAtLeast(0f).coerceAtMost(availableHeight)
                    val remaining = VerticalTextLayout(
                        text = text,
                        start = firstEnd,
                        end = end,
                        paint = paint,
                        height = (availableHeight - remainingIndent).coerceAtLeast(0f),
                    )
                    repeat(remaining.lineCount) { index ->
                        val columnEnd = remaining.getLineEnd(index)
                        columns.add(column(
                            remaining.getLineStart(index),
                            columnEnd,
                            remainingIndent,
                            if (columnEnd == end) nextStart else columnEnd,
                        ))
                    }
                }
            }
            start = nextStart
        }
        return VerticalParagraphLayout(columns).also {
            cachedHeight = height
            cachedLayout = it
        }
    }

    private fun column(start: Int, end: Int, indent: Float, nextStart: Int): VerticalParagraphColumn {
        val layout = VerticalTextLayout(
            text = text,
            start = start,
            end = end,
            paint = paint,
            height = Float.POSITIVE_INFINITY,
            orientation = TextOrientation.Mixed,
        )
        return VerticalParagraphColumn(
            layout, columnWidth(layout.width), indent, layout.contentHeight, end, nextStart,
            start, text, paint,
        )
    }

    private fun columnWidth(glyphWidth: Float): Float =
        lineHeight.toPixels(density, glyphWidth).coerceAtLeast(glyphWidth)
}

@SuppressLint("RestrictedApi")
internal class VerticalParagraphColumn(
    val layout: VerticalTextLayout?,
    val width: Float,
    val top: Float,
    val height: Float,
    val end: Int,
    val nextStart: Int,
    val start: Int = end,
    private val text: Spanned? = null,
    private val paint: TextPaint? = null,
) {
    private val boundaries by lazy {
        val iterator = BreakIterator.getCharacterInstance().apply { setText(checkNotNull(text).toString()) }
        buildList {
            add(start)
            var offset = iterator.following(start)
            while (offset in (start + 1) until end) {
                add(offset)
                offset = iterator.next()
            }
            add(end)
        }
    }
    private val advances = mutableMapOf<Int, Float>()

    fun yAt(offset: Int): Float = top + when (offset) {
        start -> 0f
        end -> height
        else -> advances.getOrPut(offset) {
            VerticalTextLayout(
                text = checkNotNull(text),
                start = start,
                end = offset,
                paint = checkNotNull(paint),
                height = Float.POSITIVE_INFINITY,
                orientation = TextOrientation.Mixed,
            ).contentHeight.coerceIn(0f, height)
        }
    }

    fun offsetAt(y: Float, nearest: Boolean): Int {
        val index = boundaries.binarySearch { offset -> yAt(offset).compareTo(y) }
        if (index >= 0) return boundaries[index.coerceAtMost(boundaries.lastIndex - if (nearest) 0 else 1)]
        val after = (-index - 1).coerceIn(1, boundaries.lastIndex)
        val before = boundaries[after - 1]
        val next = boundaries[after]
        return if (nearest && y - yAt(before) > yAt(next) - y) next else before
    }
}

/**
 * 竖排段落的断列与测绘结果
 * 提供绘制、文字命中查询和选区矩形计算
 *
 * @since Api 4
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class VerticalParagraphLayout internal constructor(private val columns: List<VerticalParagraphColumn>) {
    /**
     * 所有文本列占用的总宽度, 单位为px
     *
     * @since Api 4
     */
    val width: Int = ceil(columns.sumOf { it.width.toDouble() }).toInt()
    /**
     * 最高文本列占用的实际高度, 包含缩进, 单位为px
     *
     * @since Api 4
     */
    val height: Int = ceil(columns.maxOfOrNull { it.top + it.height } ?: 0f).toInt()

    internal fun splitOffsets(availableWidth: Int): Pair<Int, Int>? {
        if (width <= availableWidth) return null
        var used = 0.0
        val lastFitting = columns.takeWhile { column ->
            used += column.width
            ceil(used) <= availableWidth
        }.lastOrNull() ?: return null
        return lastFitting.end to lastFitting.nextStart
    }

    /**
     * 查询段落内指定位置对应的文本偏移
     *
     * @param position 相对于段落左上角的位置, 单位为px
     * @param availableWidth 段落的可用宽度, 用于从右至左书写时定位文本列, 单位为px
     * @param orientation 竖排书写方向
     * @param nearest 是否允许将列外位置映射到最近的字符边界
     *
     * @return 原文本中的UTF-16偏移, 未命中文本列时返回null
     *
     * @since Api 4
     */
    fun offsetAt(
        position: Offset,
        availableWidth: Float,
        orientation: WriteMode,
        nearest: Boolean,
    ): Int? {
        val x = if (orientation == WriteMode.VerticalRTL) availableWidth - position.x else position.x
        var result: Int? = null
        var bestDistance = Float.POSITIVE_INFINITY
        forEachColumn { column, offset ->
            if (column.layout != null) {
                val right = offset + column.width
                val distance = maxOf(offset - x, x - right, 0f)
                if (distance < bestDistance && (nearest ||
                        (distance == 0f && position.y >= column.top && position.y <= column.top + column.height))
                ) {
                    bestDistance = distance
                    result = column.offsetAt(position.y, nearest)
                }
            }
        }
        return result
    }

    /**
     * 计算文本选区在各列中占用的矩形
     *
     * @param range 原文本中的UTF-16选区范围
     * @param availableWidth 段落的可用宽度, 单位为px
     * @param orientation 竖排书写方向
     *
     * @return 相对于段落左上角的选区矩形列表, 单位为px
     *
     * @since Api 4
     */
    fun selectionRects(range: TextRange, availableWidth: Float, orientation: WriteMode): List<Rect> =
        buildList {
            forEachColumn { column, offset ->
                val start = maxOf(range.min, column.start)
                val end = minOf(range.max, column.end)
                if (start < end) {
                    val left = columnLeft(offset, column.width, availableWidth, orientation)
                    add(Rect(left, column.yAt(start), left + column.width, column.yAt(end)))
                }
            }
        }

    /**
     * 在当前画布坐标系中绘制竖排段落
     *
     * @param canvas 用于绘制的Android画布
     * @param availableWidth 段落的可用宽度, 单位为px
     * @param orientation 竖排书写方向
     *
     * @since Api 4
     */
    fun draw(canvas: Canvas, availableWidth: Float, orientation: WriteMode) {
        forEachColumn { column, offset ->
            val left = columnLeft(offset, column.width, availableWidth, orientation)
            column.layout?.let { layout ->
                layout.draw(canvas, left + (column.width + layout.width) / 2f, column.top)
            }
        }
    }

    private inline fun forEachColumn(block: (VerticalParagraphColumn, Float) -> Unit) {
        var offset = 0f
        columns.forEach { column ->
            block(column, offset)
            offset += column.width
        }
    }

    private fun columnLeft(offset: Float, width: Float, availableWidth: Float, orientation: WriteMode) =
        if (orientation == WriteMode.VerticalRTL) availableWidth - offset - width else offset
}
