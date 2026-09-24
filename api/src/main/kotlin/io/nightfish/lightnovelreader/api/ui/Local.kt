package io.nightfish.lightnovelreader.api.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.sp
import io.nightfish.lightnovelreader.api.content.component.ComponentRender

/**
 * 返回当前 Navigation 3 页面。
 *
 * @since Api 4
 */
val LocalPopBackStack = compositionLocalOf<() -> Boolean> {
    error("CompositionLocal LocalPopBackStack not present")
}

/**
 * 当前阅读器的样式配置
 *
 * @since Api 2
 */
val LocalReaderStyle = compositionLocalOf {
    ReaderStyle(
        fontSize = 15.sp,
        lineHeight = 7.sp,
        fontWeight = FontWeight.W500,
        textColor = Color.Unspecified,
        textDarkColor = Color.Unspecified,
        spacingBeforeParagraph = 0.sp,
        spacingAfterParagraph = 0.sp,
        textIndent = TextIndent(
            firstLine = 30.sp,
        ),
        fontUri = null,
        letterSpacing = 0.2.sp
    )
}

/**
 * 当前应用使用的文本语言区域列表
 * 用于控制Compose文本排版的语言环境
 *
 * @since Api 2
 */
val LocalTextLocaleList = compositionLocalOf {
    LocaleList(Locale.current)
}

val LocalComponentRender = compositionLocalOf<ComponentRender> {
    error("CompositionLocal LocalComponentRender not present")
}

/**
 * 将应用语言设置字符串转换为Compose文本排版所需的[LocaleList]
 *
 * @param appLocale 应用语言字符串, 格式为"语言-地区"(如"zh-CN")
 *
 * @return 对应的[LocaleList]
 *
 * @since Api 2
 */
fun appLocaleToTextLocaleList(appLocale: String): LocaleList {
    val parts = appLocale.split("-")
    val language = parts.getOrNull(0).orEmpty()
    val region = parts.getOrNull(1).orEmpty()

    val tag = buildString {
        append(language.ifBlank { "en" })
        if (region.isNotBlank()) {
            append("-")
            append(region)
        }
    }

    return LocaleList(Locale(tag))
}