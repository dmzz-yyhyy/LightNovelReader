package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.Screen
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.ui.components.NavItem
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val ReadingScreenInfo = NavItem(
    route = Screen.Home.Reading.route,
    drawable = R.drawable.animated_book,
    label = R.string.nav_reading
)

@Composable
fun ReadingScreen(
    onOpenBook: (Int) -> Unit,
    topBar: (@Composable () -> Unit) -> Unit,
    viewModel: ReadingViewModel = hiltViewModel()
) {
    val readingBooks = viewModel.uiState.recentReadingBooks
    topBar { TopBar() }
    if (viewModel.uiState.isLoading) {
        Loading()
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp, 0.dp, 16.dp, 0.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text(
                modifier = Modifier.padding(0.dp, 8.dp, 0.dp, 8.dp),
                text = "最近阅读 (${readingBooks.size})",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W700,
                    lineHeight = 16.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LargeBookCard(readingBooks[0])
        }
        items(readingBooks.subList(1, readingBooks.size - 1)) {
            SimpleBookCard(it, onOpenBook)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.nav_reading),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        modifier = Modifier.fillMaxWidth(),
        actions = {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.ui_more)
                )
            }
        }
    )
}

@Composable
private fun SimpleBookCard(book: ReadingBook, onOpenBook: (Int) -> Unit) {
    Row(Modifier
        .fillMaxWidth().height(120.dp)) {
        Cover(81.dp, 120.dp, book.coverUrl)
        Column(Modifier.fillMaxSize().padding(16.dp, 0.dp, 0.dp, 0.dp)) {
            Column(Modifier.fillMaxWidth().height(96.dp)) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.W600
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "作者: ${book.author} / 文库: ${book.publishingHouse}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = book.description,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxHeight().width(259.dp).align(Alignment.CenterStart)) {
                    Icon(
                        modifier = Modifier.size(16.dp)
                            .align(Alignment.CenterVertically),
                        painter = painterResource(id = R.drawable.outline_schedule_24px),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        modifier = Modifier.align(Alignment.CenterVertically),
                        text = " ${formTime(book.lastReadTime)}"
                                + " · 读了${(book.totalReadTime / 60)}分钟"
                                + " · ${(book.readingProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.W400
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    modifier = Modifier.size(24.dp).align(Alignment.CenterEnd),
                    onClick = {onOpenBook(book.id)}) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_forward_24px),
                        contentDescription = "enter",
                        tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun LargeBookCard(book: ReadingBook) {
    Box(Modifier.padding(0.dp, 8.dp, 0.dp, 8.dp).fillMaxWidth().height(194.dp)) {
        Row {
            Cover(118.dp, 178.dp, book.coverUrl)
            Column(Modifier.padding(24.dp, 0.dp, 0.dp, 0.dp)) {
                Text(
                    modifier = Modifier.fillMaxWidth().height(66.dp),
                    text = book.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.W600
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    modifier = Modifier.fillMaxWidth().height(66.dp),
                    text = book.description,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.W500
                    ),
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis
                )
                Button(onClick = {}) {
                    Text(
                        text = "继续阅读: ${book.lastReadChapterTitle}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.W700
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@SuppressLint("NewApi")
private fun formTime(time: LocalDateTime): String {
    val now = LocalDateTime.now()
    val dayDiff = now.dayOfYear - time.dayOfYear
    val hourDiff = now.hour - time.hour
    val minuteDiff = now.minute - time.minute

    return when {
        time.isAfter(now) -> {
            val formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")
            time.format(formatter)
        }
        time.year < now.year -> "去年"
        dayDiff in 1..3 -> {
            val prefix = when (dayDiff) {
                1 -> "昨天"
                2 -> "前天"
                3 -> "大前天"
                else -> ""
            }
            if (dayDiff <=2) {
                "$prefix ${time.hour}:${time.minute}"
            } else {
                prefix
            }
        }
        hourDiff in 1..24 -> "$hourDiff 小时前"
        minuteDiff == 0 -> "刚刚"
        minuteDiff in 1 until 60 -> "$minuteDiff 分钟前"
        else -> "很久之前"
    }
}