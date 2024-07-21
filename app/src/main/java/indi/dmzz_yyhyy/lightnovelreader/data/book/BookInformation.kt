package indi.dmzz_yyhyy.lightnovelreader.data.book

import androidx.compose.runtime.Stable
import java.time.LocalDateTime

@Stable
data class BookInformation(
    val id: Int,
    val title: String,
    val coverUrl: String,
    val author: String,
    val description: String,
    val publishingHouse: String,
    val wordCount: Int,
    val lastUpdated: LocalDateTime,
    val isComplete: Boolean
) {
    companion object {
        fun empty(): BookInformation = BookInformation(
                -1,
                "",
                "",
                "",
                "",
                "",
                0,
                LocalDateTime.MIN,
                false
            )
    }
}
