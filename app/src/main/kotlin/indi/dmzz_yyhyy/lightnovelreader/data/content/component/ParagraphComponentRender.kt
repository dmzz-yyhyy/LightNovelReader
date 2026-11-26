package indi.dmzz_yyhyy.lightnovelreader.data.content.component

import android.content.Context
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.content.component.render.AbstractContentComponentRender
import io.nightfish.lightnovelreader.api.content.component.data.ParagraphComponentData
import io.nightfish.lightnovelreader.api.content.component.render.cap.SizeMeasurable
import io.nightfish.lightnovelreader.api.text.layout.resolveVerticalTypefaces
import io.nightfish.lightnovelreader.api.text.layout.verticalParagraphText
import io.nightfish.lightnovelreader.api.ui.LocalReaderStyle
import io.nightfish.lightnovelreader.api.ui.ReaderStyle
import io.nightfish.lightnovelreader.api.ui.theme.AppTypography
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import io.nightfish.lightnovelreader.api.userdata.UserDataRepositoryApi

class ParagraphComponentRender(
    private val userDataRepository: UserDataRepositoryApi
) : AbstractContentComponentRender<ParagraphComponentRender, ParagraphComponentData>(),
    SizeMeasurable<ParagraphComponentRender, ParagraphComponentData> {
    override val id = ParagraphComponentData.id

    override suspend fun measure(
        orientation: WriteMode,
        context: Context,
        density: Density,
        crossSize: Int,
        data: ParagraphComponentData
    ): Int {
        val size = measureParagraph(orientation, context, density, crossSize, data)
        return if (orientation == WriteMode.Horizontal) size.height else size.width
    }

    override suspend fun measure(
        orientation: WriteMode,
        availableSpace: IntSize,
        context: Context,
        density: Density,
        data: ParagraphComponentData
    ): IntSize {
        val crossSize = if (orientation == WriteMode.Horizontal) availableSpace.width else availableSpace.height
        return measureParagraph(orientation, context, density, crossSize, data)
    }

    private suspend fun measureParagraph(
        orientation: WriteMode,
        context: Context,
        density: Density,
        crossSize: Int,
        data: ParagraphComponentData
    ): IntSize {
        require(crossSize >= 0) { "Available cross-axis size must not be negative" }
        if (crossSize == 0) return IntSize.Zero

        val readerStyle = readReaderStyle()
        val baseStyle = AppTypography.bodyMedium
        val text = data.toAnnotatedString(readerStyle, baseStyle, data.index != 1)
        val contentSize = when (orientation) {
            WriteMode.Horizontal -> componentTextMeasurer(context, density).measure(
                text = text,
                style = baseStyle,
                constraints = Constraints(maxWidth = crossSize),
            ).size.height
            WriteMode.VerticalRTL, WriteMode.VerticalLTR -> {
                val typefaces = resolveVerticalTypefaces(text, baseStyle, createFontFamilyResolver(context))
                verticalParagraphText(text, baseStyle, density, typefaces).layout(crossSize).width
            }
        }
        val length = contentSize + with(density) {
            readerStyle.spacingBeforeParagraph.toDp().roundToPx() +
                readerStyle.spacingAfterParagraph.toDp().roundToPx()
        }
        val drawsContent = text.text.isNotBlank() || text.spanStyles.any { span ->
            span.item.background.isSpecified ||
                span.item.textDecoration?.let { it != TextDecoration.None } == true
        }
        // A blank paragraph occupies space on the reading axis without drawing anything.
        val measuredCrossSize = if (drawsContent) crossSize else 0
        return if (orientation == WriteMode.Horizontal) {
            IntSize(measuredCrossSize, length)
        } else {
            IntSize(length, measuredCrossSize)
        }
    }

    private suspend fun readReaderStyle(): ReaderStyle {
        val default = ReaderStyle()
        suspend fun value(path: UserDataPath, fallback: Float) =
            userDataRepository.floatUserData(path.path).getOrDefault(fallback)
        return default.copy(
            fontSize = value(UserDataPath.Reader.FontSize, default.fontSize.value).sp,
            fontWeight = FontWeight(value(UserDataPath.Reader.FontWeigh, default.fontWeight.weight.toFloat()).toInt()),
            lineHeight = value(UserDataPath.Reader.LineHeight, default.lineHeight.value).em,
            letterSpacing = value(UserDataPath.Reader.LetterSpacing, default.letterSpacing.value).sp,
            spacingBeforeParagraph = value(UserDataPath.Reader.SpacingBeforeParagraph, default.spacingBeforeParagraph.value).sp,
            spacingAfterParagraph = value(UserDataPath.Reader.SpacingAfterParagraph, default.spacingAfterParagraph.value).sp,
            textIndent = TextIndent(firstLine = value(UserDataPath.Reader.FirstLineTextIndent, default.textIndent.firstLine.value).em),
            fontUri = userDataRepository.uriUserData(UserDataPath.Reader.FontUri.path).get(),
        )
    }

    @Composable
    override fun Content(
        modifier: Modifier,
        orientation: WriteMode,
        data: ParagraphComponentData
    ) {
        val density = LocalDensity.current
        val readerStyle = LocalReaderStyle.current
        val baseStyle = MaterialTheme.typography.bodyMedium
        val text = data.toAnnotatedString(
            readerStyle,
            baseStyle,
            data.index != 1,
            LocalAppTheme.current.isDark,
        )
        val before = with(density) { readerStyle.spacingBeforeParagraph.toDp() }
        val after = with(density) { readerStyle.spacingAfterParagraph.toDp() }
        when (orientation) {
            WriteMode.Horizontal -> Text(
                text = text,
                style = baseStyle,
                modifier = modifier.padding(top = before, bottom = after),
            )
            WriteMode.VerticalRTL, WriteMode.VerticalLTR -> VerticalParagraph(
                text = text,
                style = baseStyle,
                orientation = orientation,
                modifier = modifier.absolutePadding(
                    left = if (orientation == WriteMode.VerticalRTL) after else before,
                    right = if (orientation == WriteMode.VerticalRTL) before else after,
                ),
            )
        }
    }
}
