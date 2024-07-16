package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReadingScreen(
    onOpenBook: (Int) -> Unit,
    topBar: (@Composable () -> Unit) -> Unit
) {
    // test data
    val book = ReadingBook(
        BookInformation(
            0,
            "不时轻声地以俄语遮羞的邻座艾莉同学",
            "http://img.wenku8.com/image/2/2930/2930s.jpg",
            "灿灿SUN",
            "「И наменятоже обрати внимание.」\n" +
                    "「啊？你说什么？」\n" +
                    "「没有啊？我只是说『这家伙真的很蠢』。」\n" +
                    "「可以别用俄语骂我吗？」\n" +
                    "坐在我旁边座位的绝世银发美少女艾莉，轻轻露出夸耀胜利的笑容……\n" +
                    "然而实际上不是这样。她刚才说的俄语是：「理我一下啦！」\n" +
                    "其实我──久世政近的俄语听力达到母语水准。\n" +
                    "毫不知情的艾莉同学，今天也以甜蜜的俄语表现娇羞的一面，害我止不住笑意？\n" +
                    "全校学生心目中的女神，才貌双全俄罗斯美少女和我的青春恋爱喜剧！\n",
            listOf(),
            "角川文库",
            1046232,
            LocalDateTime.now(),
            false),
        UserReadingData(
            LocalDateTime.now(),
            130,
            0.8,
            100,
            "短篇 画集附录短篇 后来被欺负得相当惨烈",
            0.8,
        )
    )
    val readingBooks = listOf(
        book.copy(),
        book.copy(),
        book.copy(),
        book.copy(),
        book.copy(),
        book.copy(),
        book.copy(),
        book.copy(),
        book.copy(),
        book.copy(),
        book.copy()
    )


    topBar { TopBar() }
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
            SimpleBookCard(it)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar() {
    TopAppBar(
        title = { Text(
            text = "Reading",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        ) },
        actions = { IconButton(
            onClick = {}) {
            Icon(painterResource(id = R.drawable.more_vert_24px), "more")
        }
        }
    )
}

@Composable
private fun Cover(width: Dp, height: Dp, url: String) {
    Box(modifier = Modifier
        .size(width, height)
        .clip(RoundedCornerShape(14.dp))) {
        Box(Modifier
            .size(width, height)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size((width.times(0.33898306f))).align(Alignment.Center)
            )
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url
                )
                .crossfade(true)
                .build(),
            contentDescription = "cover",
            modifier = Modifier
                .size(width, height)
        )
    }
}

@Composable
private fun SimpleBookCard(book: ReadingBook) {
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
                    maxLines = 1
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
                    maxLines = 1
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
                    onClick = {}) {
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = {}) {
                    Text(
                        text = "继续阅读: ${book.lastReadChapterTitle}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.W700
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@SuppressLint("NewApi")
private fun formTime(time: LocalDateTime): String {
    val dayAndPrefixList = listOf("", "昨天", "前天", "大前天")
    var prefix = ""
    if (time.year < LocalDateTime.now().year) {
        prefix = "去年"
        return prefix
    }
    if (LocalDateTime.now().dayOfYear - time.dayOfYear in 1..3) {
        prefix = dayAndPrefixList[LocalDateTime.now().dayOfYear - time.dayOfYear]
        if (LocalDateTime.now().dayOfYear - time.dayOfYear == 1) {
            return prefix + "${time.hour}:${time.minute}"
        }
        return prefix
    }
    if (time.hour - LocalDateTime.now().hour in 1..2) {
        return "${time.hour - LocalDateTime.now().hour}小时前"
    }
    if (time.minute - LocalDateTime.now().minute == 0) {
        return "刚刚"
    }
    return "${time.minute - LocalDateTime.now().minute}分钟前"
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(device = "id:pixel_8_pro")
@Composable
fun ReadingScreenPreview() {
    val book = ReadingBook(
        BookInformation(
            0,
            "不时轻声地以俄语遮羞的邻座艾莉同学",
            "http://img.wenku8.com/image/2/2930/2930s.jpg",
            "灿灿SUN",
            "「И наменятоже обрати внимание.」\n" +
                    "「啊？你说什么？」\n" +
                    "「没有啊？我只是说『这家伙真的很蠢』。」\n" +
                    "「可以别用俄语骂我吗？」\n" +
                    "坐在我旁边座位的绝世银发美少女艾莉，轻轻露出夸耀胜利的笑容……\n" +
                    "然而实际上不是这样。她刚才说的俄语是：「理我一下啦！」\n" +
                    "其实我──久世政近的俄语听力达到母语水准。\n" +
                    "毫不知情的艾莉同学，今天也以甜蜜的俄语表现娇羞的一面，害我止不住笑意？\n" +
                    "全校学生心目中的女神，才貌双全俄罗斯美少女和我的青春恋爱喜剧！\n",
            listOf(),
            "角川文库",
            1046232,
            LocalDateTime.now(),
            false),
        UserReadingData(
            LocalDateTime.now(),
            130,
            0.8,
            100,
            "短篇 画集附录短篇 后来被欺负得相当惨烈",
            0.8,
        )
    )
    SimpleBookCard(book)
}

