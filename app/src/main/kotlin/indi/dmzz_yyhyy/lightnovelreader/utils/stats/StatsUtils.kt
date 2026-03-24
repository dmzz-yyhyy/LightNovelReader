package indi.dmzz_yyhyy.lightnovelreader.utils.stats

import androidx.compose.ui.graphics.Color
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.BookRecord
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.TimeBarItem
import io.nightfish.lightnovelreader.api.book.BookInformation
import kotlin.collections.map

fun generateTimeBarItems(
    recordList: List<BookRecord>,
    bookInformationMap: Map<String, BookInformation>,
    colorMap: Map<String, Color>,
    topLimit: Int = 8
): List<TimeBarItem> {

    if (recordList.isEmpty()) return emptyList()

    val groupedTime = recordList
        .groupBy { it.bookId }
        .mapValues { (_, list) ->
            list.sumOf { it.seconds }
        }
        .toList()
        .sortedByDescending { it.second }

    val topBooks = groupedTime.take(topLimit)
    val othersTime = groupedTime.drop(topLimit).sumOf { it.second }

    val items = buildList {
        addAll(
            topBooks.map { (bookId, time) ->
                TimeBarItem(
                    title = bookInformationMap[bookId]?.title ?: "Unknown",
                    timeSeconds = time,
                    color = colorMap[bookId] ?: Color.Gray
                )
            }
        )

        if (othersTime > 0) {
            add(
                TimeBarItem(
                    title = "Others",
                    timeSeconds = othersTime,
                    color = Color.Gray
                )
            )
        }
    }

    return items
}