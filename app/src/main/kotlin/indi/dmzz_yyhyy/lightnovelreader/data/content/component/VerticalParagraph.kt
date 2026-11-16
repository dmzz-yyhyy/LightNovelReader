package indi.dmzz_yyhyy.lightnovelreader.data.content.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.text.layout.VerticalParagraphLayout
import io.nightfish.lightnovelreader.api.text.layout.resolveVerticalTypefaces
import io.nightfish.lightnovelreader.api.text.layout.verticalParagraphText

@Composable
internal fun VerticalParagraph(
    text: AnnotatedString,
    style: TextStyle,
    orientation: WriteMode,
    modifier: Modifier,
) {
    val density = LocalDensity.current
    val resolver = LocalFontFamilyResolver.current
    // Read resolver states during composition so asynchronously loaded fonts trigger a new layout.
    val typefaces = resolveVerticalTypefaces(text, style, resolver)
    val paragraph = remember(text, style, density, typefaces) {
        verticalParagraphText(text, style, density, typefaces)
    }
    var textLayout by remember(paragraph) { mutableStateOf<VerticalParagraphLayout?>(null) }
    Layout(
        content = {},
        modifier = modifier
            .semantics { this.text = text }
            .clipToBounds()
            .drawBehind {
                drawIntoCanvas { canvas ->
                    textLayout?.draw(canvas.nativeCanvas, size.width, orientation)
                }
            },
    ) { _, constraints ->
        val measured = paragraph.layout(constraints.maxHeight)
        textLayout = measured
        layout(
            constraints.constrainWidth(measured.width),
            constraints.constrainHeight(measured.height),
        ) {}
    }
}
