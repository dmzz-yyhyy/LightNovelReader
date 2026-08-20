package io.nightfish.lightnovelreader.api.ui

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import java.io.File

/**
 * 阅读器样式配置
 *
 * @param fontSize 字体大小
 * @param fontWeight 字体粗细程度(100~900)
 * @param lineHeight 行间距附加大小
 * @param letterSpacing 字间距
 * @param textColor 亮色模式下的文本颜色
 * @param textDarkColor 深色模式下的文本颜色
 * @param textIndent 文本缩进
 * @param spacingBeforeParagraph 段前间距
 * @param spacingAfterParagraph 段后间距
 * @param fontUri 自定义字体Uri
 *
 * @since Api 2
 */
data class ReaderStyle(
    val fontSize: TextUnit = 14.sp,
    val fontWeight: FontWeight = FontWeight.Normal,
    val lineHeight: TextUnit = 1.4.em,
    val letterSpacing: TextUnit = 0.2.sp,
    val textColor: Color = Color.Black,
    val textDarkColor: Color = Color.White,
    val spacingBeforeParagraph: TextUnit = 0.sp,
    val spacingAfterParagraph: TextUnit = 0.sp,
    val textIndent: TextIndent = TextIndent(
        firstLine = 2.em,
    ),
    val fontUri: Uri? = null,
) {
    fun toParagraphStyle(): ParagraphStyle =
        ParagraphStyle(
            lineHeight = lineHeight,
            textIndent = textIndent
        )

    fun toSpanStyle(
        darkMode: Boolean
    ): SpanStyle {
        val fontFamily = fontUri
            ?.takeUnless { it == Uri.EMPTY }
            ?.path
            ?.takeIf(String::isNotBlank)
            ?.let(::File)
            ?.takeIf(File::isFile)
            ?.let { runCatching { FontFamily(Font(it)) }.getOrNull() }
            ?: FontFamily.SansSerif

        return SpanStyle(
            color = if (darkMode) textDarkColor else textColor,
            fontSize = fontSize,
            fontWeight = fontWeight,
            fontFamily = fontFamily,
            letterSpacing = letterSpacing,
        )
    }
}
