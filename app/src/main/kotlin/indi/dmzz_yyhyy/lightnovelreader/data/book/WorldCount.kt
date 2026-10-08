package indi.dmzz_yyhyy.lightnovelreader.data.book

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import indi.dmzz_yyhyy.lightnovelreader.R
import io.nightfish.lightnovelreader.api.book.WordCount
import java.text.NumberFormat
import java.util.Locale

private fun Int.numberTransform(): String =
    when {
        0 <= this && this == 1_000 -> NumberFormat.getNumberInstance(Locale.getDefault())
            .format(this)

        this in 1_000..<1_000_000 -> NumberFormat.getNumberInstance(Locale.getDefault())
            .format(this / 1000) + "K"

        1_000_000 <= this -> NumberFormat.getNumberInstance(Locale.getDefault())
            .format(this / 10000) + "W"

        else -> this.toString()
    }

@Composable
fun WordCount.get(): String =
    if (unit != null)
        if (unit!!.contains("{count}")) unit!!.replace("{count}", count.numberTransform())
        else "$count $unit"
    else if (unitResId != null)
        stringResource(unitResId!!).let {
            if (it.contains("{count}")) it.replace("{count}", count.numberTransform())
            else "$count $it"
        }
    else stringResource(R.string.book_info_word_count_kilo, count.numberTransform())

@Composable
fun volumeChapterCountText(volumeCount: Int, chapterCount: Int): String {
    val volumeText = pluralStringResource(R.plurals.n_volumes, volumeCount, volumeCount)
    return pluralStringResource(
        R.plurals.detail_info_stats_count_content,
        chapterCount,
        volumeText,
        chapterCount
    )
}
