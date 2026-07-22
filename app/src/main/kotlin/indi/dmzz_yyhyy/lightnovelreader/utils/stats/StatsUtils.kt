package indi.dmzz_yyhyy.lightnovelreader.utils.stats

import androidx.compose.ui.graphics.Color
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import indi.dmzz_yyhyy.lightnovelreader.data.statistics.BookRecord
import indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.stats.TimeBarItem
import io.nightfish.lightnovelreader.api.book.BookInformation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

fun generateTimeBarItems(
    recordList: List<BookRecord>,
    colorMap: Map<String, Color>,
    topLimit: Int = 8
): List<Flow<TimeBarItem>> {

    if (recordList.isEmpty()) return emptyList()

    val groupedTime = recordList
        .groupBy { it.bookId to it.bookInformationFlow }
        .mapValues { (_, list) ->
            list.sumOf { it.seconds }
        }
        .toList()
        .sortedByDescending { it.second }

    val topBooks = groupedTime.take(topLimit)
    val othersTime = groupedTime.drop(topLimit).sumOf { it.second }

    val items = buildList {
        addAll(
            topBooks.map { (pair, time) ->
                pair.second.map {
                    TimeBarItem(
                        title = it.map(BookInformation::title).getOrElse { "Unknown" },
                        timeSeconds = time,
                        color = colorMap[pair.first] ?: Color.Gray
                    )
                }
            }
        )

        if (othersTime > 0) {
            add(
                flow {
                    emit(
                        TimeBarItem(
                            title = "Others",
                            timeSeconds = othersTime,
                            color = Color.Gray
                        )
                    )
                }
            )
        }
    }

    return items
}