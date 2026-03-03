package indi.dmzz_yyhyy.lightnovelreader.data.content.component

import android.content.Context
import android.net.Uri
import android.util.DisplayMetrics
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.book.reader.content.componet.SimpleTextComponentContent
import indi.dmzz_yyhyy.lightnovelreader.utils.loadReaderFontFamilySafe
import indi.dmzz_yyhyy.lightnovelreader.utils.rememberReaderFontFamily
import io.nightfish.lightnovelreader.api.content.component.AbstractDivisibleContentComponent
import io.nightfish.lightnovelreader.api.content.component.SimpleTextComponentData
import io.nightfish.lightnovelreader.api.ui.LocalReaderStyle
import io.nightfish.lightnovelreader.api.ui.theme.AppTypography
import io.nightfish.lightnovelreader.api.userdata.UriUserData
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import io.nightfish.lightnovelreader.api.userdata.UserDataRepositoryApi

class SimpleTextComponent(
    data: SimpleTextComponentData,
    val userDataRepositoryApi: UserDataRepositoryApi,
    val context: Context
): AbstractDivisibleContentComponent<SimpleTextComponent, SimpleTextComponentData>(data) {

    val fontSizeUserData = userDataRepositoryApi.floatUserData(UserDataPath.Reader.FontSize.path)
    val fontLineHeightUserData = userDataRepositoryApi.floatUserData(UserDataPath.Reader.FontLineHeight.path)
    val fontWeightUserData = userDataRepositoryApi.floatUserData(UserDataPath.Reader.FontWeigh.path)
    val textColorUserData = userDataRepositoryApi.colorUserData(UserDataPath.Reader.TextColor.path)
    val textDarkColorUserData = userDataRepositoryApi.colorUserData(UserDataPath.Reader.TextDarkColor.path)
    val fontFamilyUriUserData = userDataRepositoryApi.uriUserData(UserDataPath.Reader.FontFamilyUri.path)
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

    override val id = SimpleTextComponentData.ID

    @Composable
    override fun Content(modifier: Modifier) {
        val combinedStyle = LocalReaderStyle.current
        SimpleTextComponentContent(
            modifier = modifier,
            text = data.text,
            fontSize = combinedStyle.fontSize.sp,
            fontLineHeight = combinedStyle.fontLineHeight.sp,
            fontWeight = FontWeight(combinedStyle.fontWeight.toInt()),
            fontFamily = rememberReaderFontFamily(fontFamilyUriUserData),
            color = readerTextColor(combinedStyle.textColor, combinedStyle.textDarkColor)
        )
    }

    @Composable
    private fun readerTextColor(textColor: Color, textDarkColor: Color): Color {
        val localTheme = LocalAppTheme.current
        val isDark = localTheme.isDark
        val onSurface = localTheme.colorScheme.onSurface

        val color = remember(isDark, textColor, textDarkColor, onSurface) {
            when {
                isDark && textDarkColor.isUnspecified -> onSurface
                !isDark && textColor.isUnspecified -> onSurface
                isDark -> textDarkColor
                else -> textColor
            }
        }

        return color
    }

    override fun split(
        height: Int,
        width: Int
    ): List<SimpleTextComponent> {
        val fontSize = fontSizeUserData.getOrDefault(15f)
        val fontLineHeight = fontLineHeightUserData.getOrDefault(7f)
        val fontWeigh = fontWeightUserData.getOrDefault(500f)
        return textMeasurer.measure(
            text = data.text,
            style = AppTypography.bodyMedium.copy(
                fontSize = fontSize.sp,
                lineHeight = (fontLineHeight + fontSize).sp,
                fontWeight = FontWeight(fontWeigh.toInt()),
                fontFamily = readerFontFamily(fontFamilyUriUserData),
            ),
            constraints = Constraints(maxHeight = height, maxWidth = width),
        )
            .getSlipString(data.text, width, height)
            .map { SimpleTextComponent(SimpleTextComponentData(it), userDataRepositoryApi, context) }
    }

    fun readerFontFamily(fontFamilyUriUserData: UriUserData): FontFamily? {
        val uri = fontFamilyUriUserData.getOrDefault(Uri.EMPTY)
        val fontFamily = loadReaderFontFamilySafe(uri)
        return fontFamily
    }

    fun TextLayoutResult.getSlipString(text: String, width: Int, height: Int): List<String> {
        val result: MutableList<String> = mutableListOf()
        var lastLine = 0
        fun getNotOverflowText(startLine: Int): String {
            fun getNotOverflowLine(): Int {
                val startHeight = getLineTop(startLine)
                fun isLineOverflow(line: Int): Boolean =
                    getLineBottom(line) > height + startHeight

                var checkLine = getLineForOffset(
                    getOffsetForPosition(
                        Offset(
                            width.toFloat(),
                            startHeight + height
                        )
                    )
                )
                while (isLineOverflow(checkLine)) checkLine--
                return checkLine
            }

            val startTextOffset = getLineStart(startLine)
            lastLine = getNotOverflowLine()
            val endTextOffset = getLineEnd(lastLine)
            lastLine++
            return text.slice(startTextOffset..<endTextOffset)
        }
        while (lastLine < this.lineCount) {
            getNotOverflowText(lastLine).let(result::add)
        }
        return result.mapIndexedNotNull { index, string ->
            when (index) {
                0 if string.isBlank() -> null
                result.size - 1 if string.isBlank() -> null
                else -> string
            }
        }
    }
}