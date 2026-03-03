package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.predefinedColors

private object RegexTheme {
    val parentheses = predefinedColors[2]
    val brackets = predefinedColors[4]
    val math = predefinedColors[0]
    val symbol = predefinedColors[1]
    val control = predefinedColors[2]
    val escape = predefinedColors[3]
}

private fun AnnotatedString.Builder.colorChar(char: String, color: Color) {
    withStyle(style = SpanStyle(color = color)) {
        append(char)
    }
}

private fun AnnotatedString.Builder.escapeCheckAndColor(buffer: Char, char: Char, color: Color) {
    if (buffer != '\\') colorChar(char.toString(), color)
    else colorChar("\\$char", color = RegexTheme.escape)
}

fun regexAnnotatedString(regex: String): AnnotatedString =
    buildAnnotatedString {
        var buffer = '\r'
        var isInBrackets = false
        regex.forEachIndexed { index, char ->
            var oChar = char
            when (char) {
                '(', ')' -> escapeCheckAndColor(
                    buffer,
                    char,
                    RegexTheme.parentheses
                )

                '[', ']', '{', '}', '.' -> escapeCheckAndColor(
                    buffer,
                    char,
                    RegexTheme.brackets
                )

                '^', '-', '$', '|' -> escapeCheckAndColor(buffer, char, RegexTheme.symbol)
                '\\' -> if (buffer == '\\') {
                            colorChar("\\\\", color = RegexTheme.escape)
                            oChar = '\r'
                        }
                        else if (index == regex.length - 1) append(char)

                '*', '+' -> escapeCheckAndColor(buffer, char, RegexTheme.math)
                '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' ->
                    if (isInBrackets) colorChar(char.toString(), RegexTheme.math)
                    else append(char)

                ',' -> if (isInBrackets) colorChar(char.toString(), RegexTheme.parentheses)
                else append(char)

                else -> if (buffer == '\\')
                    if (char.isLetter()) colorChar("\\$char", color = RegexTheme.control)
                    else colorChar("\\$char", color = RegexTheme.escape)
                else append(char)
            }
            if (char == '{') isInBrackets = true
            if (char == '}') isInBrackets = false
            buffer = oChar
        }
    }

@Composable
fun RegexText(
    regex: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit) = {},
    style: TextStyle = LocalTextStyle.current
) {
    Text(
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        letterSpacing = letterSpacing,
        textDecoration = TextDecoration.None,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style,
        text = regexAnnotatedString(regex),
        fontFamily = FontFamily.Monospace
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, showSystemUi = false)
@Composable
private fun Preview() {
    Column {
        RegexText($$"\\\\[[abc][^abc][a-z][^a-z][a-zA-z]\\.a|b\\s\\S(?:.*)(...)a?a*a+a{3}a{3,6}^$aaaaadsa\\")
        Text($$"\\\\[[abc][^abc][a-z][^a-z][a-zA-z]\\.a|b\\s\\S(?:.*)(...)a?a*a+a{3}a{3,6}^$aaaaadsa\\")
    }
}