package indi.dmzz_yyhyy.lightnovelreader.ui.components.calendar.core

import androidx.compose.ui.text.intl.Locale
import java.time.DayOfWeek
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale as JavaUtilLocale

private val locale = Locale.current

fun YearMonth.displayText(short: Boolean): String {
    return month.displayText(short = short)
}

fun Month.displayText(short: Boolean = true): String {
    return getDisplayName(short, locale)
}

fun DayOfWeek.displayText(): String {
    return getDisplayName(locale)
}

fun Month.getDisplayName(short: Boolean, locale: Locale): String {
    val style = if (short) TextStyle.SHORT else TextStyle.FULL
    return getDisplayName(style, JavaUtilLocale.forLanguageTag(locale.toLanguageTag()))
}

fun DayOfWeek.getDisplayName(locale: Locale): String {
    val style = TextStyle.SHORT
    return getDisplayName(style, JavaUtilLocale.forLanguageTag(locale.toLanguageTag()))
}
