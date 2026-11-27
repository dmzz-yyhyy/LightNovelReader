package indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import io.nightfish.lightnovelreader.api.content.component.ComponentRender
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.data.cap.Divisible
import io.nightfish.lightnovelreader.api.content.component.render.cap.SizeMeasurable
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.ui.LocalComponentRender
import io.nightfish.lightnovelreader.api.ui.ReaderStyle
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.math.roundToInt

internal val WriteMode.isVertical get() = this != WriteMode.Horizontal

internal fun WriteMode.main(size: IntSize): Int = if (isVertical) size.width else size.height

internal fun WriteMode.cross(size: IntSize): Int = if (isVertical) size.height else size.width

private fun WriteMode.size(main: Int, cross: Int): IntSize =
    if (isVertical) IntSize(main, cross) else IntSize(cross, main)

internal data class MeasuredComponent(
    val data: AbstractContentComponentData,
    val size: IntSize,
    val displayedSize: IntSize = size,
) {
    val hasArea: Boolean get() = size.width > 0 && size.height > 0
}

internal class ChapterLayout(
    val chapterId: Identifier.Chapter,
    val items: List<MeasuredComponent>,
    val pages: List<List<MeasuredComponent>>,
    val offsets: List<Long>,
) {
    val length: Long get() = offsets.last()
}

internal class ChapterLayouter(
    private val render: ComponentRender,
    private val viewport: IntSize,
    private val context: Context,
    private val density: Density,
    private val style: ReaderStyle,
    private val baseStyle: TextStyle,
    private val paginated: Boolean,
) {
    private val mode = style.writeMode
    private val pageLength = mode.main(viewport)
    private val crossSize = mode.cross(viewport)

    suspend fun layout(
        chapterId: Identifier.Chapter,
        content: List<AbstractContentComponentData>
    ): ChapterLayout {
        val items = mutableListOf<MeasuredComponent>()
        val pages = mutableListOf<List<MeasuredComponent>>()
        val offsets = mutableListOf(0L)
        val page = mutableListOf<MeasuredComponent>()
        var remaining = pageLength

        fun finishPage() {
            while (page.lastOrNull()?.hasArea == false) page.removeAt(page.lastIndex)
            if (page.isNotEmpty()) pages.add(page.toList())
            page.clear()
            remaining = pageLength
        }

        fun appendToPage(component: MeasuredComponent) {
            val length = mode.main(component.displayedSize)
            if (!component.hasArea) {
                if (page.isEmpty() || length == 0) return
                if (length >= remaining) {
                    finishPage()
                    return
                }
            }
            page.add(component)
            remaining -= length
            if (remaining == 0) finishPage()
        }

        for (data in content) {
            val pending = ArrayDeque<MeasuredComponent>()
            pending.add(measure(data))
            while (pending.isNotEmpty()) {
                currentCoroutineContext().ensureActive()
                val component = pending.removeFirst()
                val available = if (paginated) remaining else pageLength
                val length = mode.main(component.displayedSize)
                if (paginated && !component.hasArea) {
                    appendToPage(component)
                    continue
                }
                val fitting = when {
                    length <= available -> component
                    else -> split(component, available, pending)
                }

                if (!paginated) {
                    val item = fitting ?: component
                    items.add(item)
                    offsets.add(offsets.last() + mode.main(item.displayedSize))
                } else if (fitting != null) {
                    appendToPage(fitting)
                } else if (page.isEmpty()) {
                    page.add(component.fitInside(viewport))
                    finishPage()
                } else {
                    finishPage()
                    pending.addFirst(component)
                }
            }
        }
        if (paginated) {
            finishPage()
            if (pages.isEmpty()) pages.add(emptyList())
        }
        return ChapterLayout(chapterId, items, pages, offsets)
    }

    private suspend fun measure(data: AbstractContentComponentData): MeasuredComponent {
        val renderer = render.getRender(data)
        val size = if (renderer is SizeMeasurable<*, *>) {
            renderer.measureAny(mode, viewport, context, density, data)
        } else {
            mode.size(renderer.measureAny(mode, context, density, crossSize, data), crossSize)
        }
        require(size.width >= 0 && size.height >= 0) {
            "Invalid measurement for ${data.id}: $size"
        }
        val constraints = if (mode.isVertical) Constraints.fitPrioritizingHeight(
            size.width, size.width, size.height, size.height
        ) else Constraints.fitPrioritizingWidth(
            size.width, size.width, size.height, size.height
        )
        val renderSize = IntSize(constraints.maxWidth, constraints.maxHeight)
        val scale = minOf(1.0, crossSize.toDouble() / mode.cross(size).coerceAtLeast(1))
        val mainSize = mode.main(size)
        val minimum = if (paginated && mainSize == 0) 0 else 1
        val displayedSize = mode.size(
            (mainSize * scale).roundToInt().coerceAtLeast(minimum),
            mode.cross(size).coerceAtMost(crossSize),
        )
        return MeasuredComponent(data, renderSize, displayedSize)
    }

    private suspend fun split(
        component: MeasuredComponent,
        available: Int,
        pending: ArrayDeque<MeasuredComponent>,
    ): MeasuredComponent? {
        val divisible = component.data as? Divisible<*> ?: return null
        if (available <= 0) return null
        val space = mode.size(available, crossSize)
        val parts = divisible.split(space.height, space.width, context, style, baseStyle)
        if (parts.size < 2 || parts.any { it === component.data }) return null

        val first = measure(parts.first())
        if (mode.main(first.displayedSize) > available) return null
        val rest = parts.drop(1).map { measure(it) }
        if (rest.any { mode.main(it.displayedSize) >= mode.main(component.displayedSize) }) return null

        rest.asReversed().forEach(pending::addFirst)
        return first
    }

    private fun MeasuredComponent.fitInside(viewport: IntSize): MeasuredComponent {
        val scale = minOf(
            1.0,
            viewport.width.toDouble() / displayedSize.width.coerceAtLeast(1),
            viewport.height.toDouble() / displayedSize.height.coerceAtLeast(1),
        )
        return copy(displayedSize = IntSize(
            (displayedSize.width * scale).roundToInt().coerceAtLeast(1),
            (displayedSize.height * scale).roundToInt().coerceAtLeast(1),
        ))
    }
}

@Composable
internal fun ChapterComponent(
    component: MeasuredComponent,
    mode: WriteMode,
    modifier: Modifier = Modifier,
) {
    val render = LocalComponentRender.current
    Layout(
        modifier = modifier.clipToBounds(),
        content = {
            if (component.hasArea) render.Component(orientation = mode, componentData = component.data)
        },
    ) { children, constraints ->
        val size = component.size
        val measured = children.map { it.measure(Constraints.fixed(size.width, size.height)) }
        val displayed = component.displayedSize
        val width = displayed.width.coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = displayed.height.coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(width, height) {
            measured.forEach {
                it.placeWithLayer(0, 0) {
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = width.toFloat() / size.width.coerceAtLeast(1)
                    scaleY = height.toFloat() / size.height.coerceAtLeast(1)
                }
            }
        }
    }
}
