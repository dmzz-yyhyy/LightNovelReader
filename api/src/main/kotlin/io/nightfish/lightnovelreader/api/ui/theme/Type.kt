package io.nightfish.lightnovelreader.api.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

private val nonTrimLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

/**
 * 应用的 Material Design 3 字体排版配置
 *
 * @since Api 2
 */
val AppTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.W600,
        lineHeightStyle = nonTrimLineHeightStyle
    ),
    displayMedium = TextStyle(
        fontSize = 19.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.W600,
        lineHeightStyle = nonTrimLineHeightStyle
    ),
    displaySmall = TextStyle(
        fontSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.W600,
        lineHeightStyle = nonTrimLineHeightStyle
    ),
    headlineLarge = TextStyle(
        fontSize = 15.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.W600,
        lineHeightStyle = nonTrimLineHeightStyle
    ),
    headlineMedium = TextStyle(
        fontSize = 13.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.W600,
        lineHeightStyle = nonTrimLineHeightStyle
    ),
    headlineSmall = TextStyle(
        fontSize = 17.sp,
        lineHeight = 17.sp * 1.5f,
        lineHeightStyle = nonTrimLineHeightStyle
    ),
    bodyLarge = TextStyle(
        fontSize = 15.sp,
        lineHeight = 24.sp,
        lineHeightStyle = nonTrimLineHeightStyle,
        lineBreak = LineBreak.Paragraph
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        lineHeightStyle = nonTrimLineHeightStyle,
        lineBreak = LineBreak.Paragraph
    ),
    bodySmall = TextStyle(
        fontSize = 13.sp,
        lineHeight = 17.sp,
        lineHeightStyle = nonTrimLineHeightStyle,
        lineBreak = LineBreak.Paragraph
    ),
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 19.sp,
        lineHeightStyle = nonTrimLineHeightStyle
    ),
    labelMedium = TextStyle(
        fontSize = 13.sp,
        lineHeight = 17.sp,
        lineHeightStyle = nonTrimLineHeightStyle
    ),
    labelSmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 15.sp,
        lineHeightStyle = nonTrimLineHeightStyle
    )
)
