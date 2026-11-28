package indi.dmzz_yyhyy.lightnovelreader.utils

import android.icu.text.DateTimePatternGenerator
import android.icu.text.SimpleDateFormat
import android.icu.util.GregorianCalendar
import android.icu.util.TimeZone
import android.icu.util.ULocale
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions.DateFormatOptions
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions.DateOrderOptions
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale

/**
 * 应用内日期的显示格式
 *
 *  - NUMERIC → 2025/12/31
 *  - WRITTEN → 2025年12月31日, December 31, 2025
 */
enum class DateFormat {
    NUMERIC, WRITTEN;

    companion object {
        fun fromString(value: String, default: DateFormat = NUMERIC): DateFormat =
            when (value.lowercase()) {
                DateFormatOptions.Numeric -> NUMERIC
                DateFormatOptions.Written -> WRITTEN
                else -> default
            }
    }
}

val appDisplayLocale: Locale
    get() = Locale.getDefault()

enum class DateOrder {
    AUTO, YMD, DMY, MDY;

    companion object {
        fun fromString(value: String): DateOrder = when (value.lowercase()) {
            DateOrderOptions.YMD -> YMD
            DateOrderOptions.DMY -> DMY
            DateOrderOptions.MDY -> MDY
            else -> AUTO
        }
    }
}

enum class DateFields { FULL, MONTH_DAY, YEAR_MONTH, YEAR }

fun formatDate(
    date: LocalDate,
    format: DateFormat,
    showYear: Boolean = true,
    order: DateOrder = DateOrder.AUTO,
    fields: DateFields = DateFields.FULL
): String {
    if (date == LocalDate.MIN) return "-"

    val locale = appDisplayLocale
    val generator = DateTimePatternGenerator.getInstance(ULocale.forLocale(locale))
    val pattern: String

    if (fields != DateFields.FULL) {
        val skeleton = when (fields) {
            DateFields.MONTH_DAY -> if (format == DateFormat.WRITTEN) "MMMMd" else "Md"
            DateFields.YEAR_MONTH -> if (format == DateFormat.WRITTEN) "yMMMM" else "yM"
            DateFields.YEAR -> "y"
            DateFields.FULL -> error("Full dates are handled below")
        }
        val explicitPattern = if (format == DateFormat.NUMERIC) {
            when (fields) {
                DateFields.MONTH_DAY -> when (order) {
                    DateOrder.AUTO -> null
                    DateOrder.DMY -> "d/M"
                    DateOrder.YMD, DateOrder.MDY -> "M/d"
                }
                DateFields.YEAR_MONTH -> when (order) {
                    DateOrder.AUTO -> null
                    DateOrder.YMD -> "yyyy/M"
                    DateOrder.DMY, DateOrder.MDY -> "M/yyyy"
                }
                DateFields.YEAR -> "yyyy"
                DateFields.FULL -> error("Full dates are handled below")
            }
        } else null
        pattern = explicitPattern ?: generator.getBestPattern(skeleton)
    } else {
        pattern = when (format) {
            DateFormat.NUMERIC -> if (order == DateOrder.AUTO) {
                generator.getBestPattern(if (showYear) "yMd" else "Md")
            } else when {
                showYear && order == DateOrder.DMY -> "d/M/yyyy"
                showYear && order == DateOrder.MDY -> "M/d/yyyy"
                showYear && order == DateOrder.YMD -> "yyyy/M/d"
                !showYear && order == DateOrder.DMY -> "d/M"
                else -> "M/d"
            }
            DateFormat.WRITTEN -> generator.getBestPattern(if (showYear) "yMMMMd" else "MMMMd")
        }
    }

    val utc = TimeZone.getTimeZone("UTC")
    val calendar = GregorianCalendar(utc, ULocale.forLocale(locale)).apply {
        gregorianChange = Date(Long.MIN_VALUE)
    }
    val formatter = SimpleDateFormat(pattern, locale).apply { this.calendar = calendar }
    return formatter.format(Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant()))
}

fun formatDate(date: LocalDate, fields: DateFields = DateFields.FULL): String = formatDate(
    date,
    DateFormat.fromString(FormattingSettings.dateFormat),
    FormattingSettings.dateShowYear,
    DateOrder.fromString(FormattingSettings.dateOrder),
    fields
)
