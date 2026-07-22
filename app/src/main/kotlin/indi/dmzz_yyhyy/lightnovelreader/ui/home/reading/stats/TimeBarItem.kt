package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
data class TimeBarItem(
    val title: String,
    val timeSeconds: Int,
    val color: Color
)

fun List<TimeBarItem>.normalize(): List<Pair<TimeBarItem, Float>> {
    val total = sumOf { it.timeSeconds }.toFloat()
    if (total <= 0f) {
        return map { it to 1f }
    }
    return map {
        it to (it.timeSeconds.toFloat() / total).coerceAtLeast(0.01f)
    }
}