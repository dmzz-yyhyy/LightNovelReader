package indi.dmzz_yyhyy.lightnovelreader.utils

import android.icu.text.MeasureFormat
import android.icu.util.MeasureUnit
import android.os.Build
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class DurationFormat(val locale: Locale = Locale.getDefault()) {
    enum class Unit {
        DAY, HOUR, MINUTE, SECOND, MILLISECOND
    }

    fun format(duration: Duration, smallestUnit: Unit = Unit.SECOND, largestUnit: Unit = Unit.DAY): String {
        val formattedStringComponents = mutableListOf<String>()
        var remainder = duration

        for (unit in Unit.entries) {
            if (unit.ordinal < largestUnit.ordinal) continue

            val component = calculateComponent(unit, remainder)

            remainder = when (unit) {
                Unit.DAY -> remainder - component.days
                Unit.HOUR -> remainder - component.hours
                Unit.MINUTE -> remainder - component.minutes
                Unit.SECOND -> remainder - component.seconds
                Unit.MILLISECOND -> remainder - component.milliseconds
            }

            val unitDisplayName = unitDisplayName(unit)

            if (component > 0) {
                val formattedComponent = android.icu.text.NumberFormat.getInstance(locale).format(component)
                formattedStringComponents.add("$formattedComponent$unitDisplayName")
            }

            if (unit == smallestUnit) {
                val formattedZero = android.icu.text.NumberFormat.getInstance(locale).format(0)
                if (formattedStringComponents.isEmpty()) {
                    formattedStringComponents.add("$formattedZero$unitDisplayName")
                }
                break
            }
        }

        return formattedStringComponents.joinToString(" ")
    }

    private fun calculateComponent(unit: Unit, remainder: Duration) = when (unit) {
        Unit.DAY -> remainder.inWholeDays
        Unit.HOUR -> remainder.inWholeHours
        Unit.MINUTE -> remainder.inWholeMinutes
        Unit.SECOND -> remainder.inWholeSeconds
        Unit.MILLISECOND -> remainder.inWholeMilliseconds
    }

    private fun unitDisplayName(unit: Unit) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val measureFormat = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.NARROW)
        when (unit) {
            Unit.DAY -> measureFormat.getUnitDisplayName(MeasureUnit.DAY)
            Unit.HOUR -> measureFormat.getUnitDisplayName(MeasureUnit.HOUR)
            Unit.MINUTE -> measureFormat.getUnitDisplayName(MeasureUnit.MINUTE)
            Unit.SECOND -> measureFormat.getUnitDisplayName(MeasureUnit.SECOND)
            Unit.MILLISECOND -> measureFormat.getUnitDisplayName(MeasureUnit.MILLISECOND)
        }
    } else {
        when (unit) {
            Unit.DAY -> "天"
            Unit.HOUR -> "小时"
            Unit.MINUTE -> "分钟"
            Unit.SECOND -> "秒"
            Unit.MILLISECOND -> "毫秒"
        }
    }
}
