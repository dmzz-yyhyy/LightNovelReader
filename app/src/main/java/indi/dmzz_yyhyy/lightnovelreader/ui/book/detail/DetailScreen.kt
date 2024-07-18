package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import android.os.Build
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.data.book.Volume
import indi.dmzz_yyhyy.lightnovelreader.ui.book.BookScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import java.time.LocalDateTime

@Composable
fun DetailScreen(
    onClickBackButton: () -> Unit,
    topBar: (@Composable () -> Unit) -> Unit,
    dialog: (@Composable () -> Unit) -> Unit

) {
    val bookInformation = BookInformation(
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
        "角川文库",
        1046232,
        LocalDateTime.now(),
        false)
    val userReadingData = UserReadingData(
        0,
        LocalDateTime.now(),
        130,
        0.8,
        100,
        "短篇 画集附录短篇 后来被欺负得相当惨烈",
        0.8,
    )
    var isShowDialog by remember { mutableStateOf(false) }

    topBar { TopBar(onClickBackButton, bookInformation.title) }
    dialog {
        if (isShowDialog) ReadFromStartDialog(
            onConfirmation = {
                isShowDialog = false
                //TODO: 从头阅读逻辑
            },
            onDismissRequest = {
                isShowDialog = false
            }
        )
    }

    LazyColumn (Modifier.padding(16.dp, 8.dp)) {
        item {
            bookCard(
                bookInformation = bookInformation,
                userReadingData = userReadingData,
                onCLickReadFromStart = { isShowDialog = true })
        }
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "介绍",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Description(bookInformation.description)
            }
        }
        item {
            Box(Modifier.fillMaxWidth().height(18.dp))
        }
        item {
            Text(
                text = "目录",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.W600,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        item {
            Box(Modifier.fillMaxWidth().height(18.dp))
        }
        for (bookVolume in emptyList<Volume>()) {
            item {
                Text(
                    text = bookVolume.volumeTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 16.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            items(bookVolume.chapters) {
                Text(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .clickable(
                            interactionSource =
                            remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            //TODO: 章节跳转
                        },
                    text = it.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W400
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
    Box(Modifier.fillMaxSize().padding(end = 31.dp, bottom = 54.dp)) {
        ExtendedFloatingActionButton(
            modifier = Modifier.align(Alignment.BottomEnd),
            onClick = { //TODO: 继续阅读
                },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.filled_menu_book_24px),
                    contentDescription = null
                )
            },
            text = { Text(text = "继续阅读") },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickBackButton: () -> Unit,
    title: String
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = onClickBackButton) {
                Icon(painterResource(id = R.drawable.arrow_back_24px), "mark")
            }
        },
        title = {
            LazyRow {
                item {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = {//TODO: 书签
                    }) {
                Icon(painterResource(id = R.drawable.outline_bookmark_24px), "mark")
            }
            IconButton(
                onClick = {}) {
                Icon(painterResource(id = R.drawable.more_vert_24px), "more")
            }
        }
    )
}

@Composable
private fun bookCard(
    bookInformation: BookInformation,
    userReadingData: UserReadingData,
    onCLickReadFromStart: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(Modifier.padding(top = 6.dp)) {
            Cover(110.dp, 165.dp, bookInformation.coverUrl)
        }
        Column {
            Text(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                text = bookInformation.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.W600,
                    fontSize = 24.sp,
                    lineHeight = 28.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
            Box(Modifier.height(4.dp))
            Text(
                modifier = Modifier.fillMaxWidth().height(18.dp),
                text = "作者: ${bookInformation.author}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
            Text(
                modifier = Modifier.fillMaxWidth().height(36.dp),
                text = "全文长度: ${bookInformation.wordCount}字",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
            Row(modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .height(57.dp),
                horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                Button(
                    modifier = Modifier.width(82.dp),
                    contentPadding = PaddingValues(12.5.dp, 10.5.dp),
                    onClick = {
                        onCLickReadFromStart()
                    }
                ) {
                    Text(text = "从头阅读",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.W500
                        ),
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = {
                        //TODO: 继续阅读
                    },
                    contentPadding = PaddingValues(12.5.dp, 10.5.dp)
                ) {
                    Text(
                        text = "继续阅读: ${userReadingData.lastReadChapterTitle.split(" ")[0]}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.W600
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun Description(description: String) {
    var isNeedExpand by remember { mutableStateOf(false) }
    var expandSummaryText by remember { mutableStateOf(false) }
    Column(Modifier
        .animateContentSize()

        .fillMaxWidth()) {
        Text(
            text = description,
            maxLines = if (expandSummaryText) Int.MAX_VALUE else 3,
            onTextLayout = {
                isNeedExpand = it.hasVisualOverflow || isNeedExpand
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.W400
            ),
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis
        )
        Box(Modifier.fillMaxWidth().height(20.dp)) {
            if (isNeedExpand && !expandSummaryText) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable(
                            interactionSource =
                            remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            expandSummaryText = !expandSummaryText
                        },
                    text = "... 展开",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W700
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (isNeedExpand) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable(
                            interactionSource =
                            remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            expandSummaryText = !expandSummaryText
                        },
                    text = "收起",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W700
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ReadFromStartDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit) {
    AlertDialog(
        title = {
            Text(
                text = "开始阅读",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.W400
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = "这将会覆盖已有的阅读进度。如果要从记录的位置继续，请点击“继续阅读”。\n" +
                        "\n" +
                        "确定要从头阅读吗？",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.W400
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text(
                    text = "确定",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W500
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(
                    text = "不确定",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.W500
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Preview
@Composable
fun preview() {
    BookScreen({}, 1)
}