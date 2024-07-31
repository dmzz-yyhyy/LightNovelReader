package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.UserReadingData
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel = hiltViewModel(),
    onClickBackButton: () -> Unit,
    onClickChapter: (Int) -> Unit,
    onClickReadFromStart: () -> Unit = {
        viewModel.uiState.bookVolumes.volumes.firstOrNull()?.chapters?.firstOrNull()?.id?.let {
            onClickChapter(it)
        }
    },
    onClickContinueReading: () -> Unit = {
        if (viewModel.uiState.userReadingData.lastReadChapterId == -1)
            viewModel.uiState.bookVolumes.volumes.firstOrNull()?.chapters?.firstOrNull()?.id?.let {
                onClickChapter(it)
            }
        else
            onClickChapter(viewModel.uiState.userReadingData.lastReadChapterId)
    },
    onClickBookMark: () -> Unit = {
    },
    onClickMore: () -> Unit = {
    },
    topBar: (@Composable (TopAppBarScrollBehavior) -> Unit) -> Unit,
    dialog: (@Composable () -> Unit) -> Unit,
    id: Int
) {
    var isShowDialog by remember { mutableStateOf(false) }
    val uiState = viewModel.uiState

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        topBar { TopBar(
            onClickBackButton = onClickBackButton,
            onClickBookMark = onClickBookMark,
            onClickMore = onClickMore,
            scrollBehavior = it,
            title = uiState.bookInformation.title
        ) }
        dialog {
            if (isShowDialog) ReadFromStartDialog(
                onConfirmation = {
                    isShowDialog = false
                    onClickReadFromStart()
                },
                onDismissRequest = {
                    isShowDialog = false
                }
            )
        }
    }
    LaunchedEffect(id) {
        viewModel.init(id)
    }
    AnimatedVisibility(
        visible =  viewModel.uiState.bookInformation.isEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Loading()
    }
    AnimatedVisibility(
        visible =  !viewModel.uiState.bookInformation.isEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LazyColumn(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 0.dp).fillMaxSize()
        ) {
            item {
                BookCard(
                    bookInformation = uiState.bookInformation,
                    userReadingData = uiState.userReadingData,
                    onCLickReadFromStart = {
                        if (uiState.userReadingData.lastReadChapterId == -1)
                            onClickReadFromStart()
                        else isShowDialog = true
                    },
                    onClickContinueReading = onClickContinueReading
                )
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "介绍",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Description(uiState.bookInformation.description)
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
            item {
                AnimatedVisibility(
                    visible =  viewModel.uiState.bookVolumes.volumes.isEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Loading()
                }
            }
            for (bookVolume in uiState.bookVolumes.volumes) {
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
                                onClickChapter(it.id)
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
                onClick = onClickContinueReading,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickBackButton: () -> Unit,
    onClickBookMark: () -> Unit,
    onClickMore: () -> Unit,
    title: String,
    scrollBehavior: TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            LazyRow {
                item {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.W400,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onClickBookMark
            ) {
                Icon(painterResource(id = R.drawable.outline_bookmark_24px), "mark")
            }
            IconButton(
                onClick = onClickMore
            ) {
                Icon(painterResource(id = R.drawable.more_vert_24px), "more")
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onClickBackButton) {
                Icon(painterResource(id = R.drawable.arrow_back_24px), "back")
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun BookCard(
    bookInformation: BookInformation,
    userReadingData: UserReadingData,
    onCLickReadFromStart: () -> Unit,
    onClickContinueReading: () -> Unit
) {
    Row(
        modifier = Modifier.padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(Modifier.padding(top = 6.dp)) {
            Cover(110.dp, 165.dp, bookInformation.coverUrl)
        }
        Column {
            Column(Modifier.fillMaxSize().height(123.dp)) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.fillMaxWidth().height(18.dp),
                    text = "全文长度: ${bookInformation.wordCount}字",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1
                )
                Text(
                    modifier = Modifier.fillMaxWidth().height(18.dp),
                    text = "状态: ${ if (bookInformation.isComplete) "已完结" else "连载中" }",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    maxLines = 1
                )
            }
            Row(modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .height(57.dp),
                horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                Button(
                    modifier = Modifier.width(82.dp),
                    contentPadding = PaddingValues(12.5.dp, 10.5.dp),
                    onClick = onCLickReadFromStart
                ) {
                    Text(text = if (userReadingData.lastReadChapterId == -1) "开始阅读" else "从头阅读",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.W500
                        ),
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
                if (userReadingData.lastReadChapterId != -1) {
                    OutlinedButton(
                        onClick = onClickContinueReading,
                        contentPadding = PaddingValues(12.5.dp, 10.5.dp)
                    ) {
                        Text(
                            text = "继续阅读: ${userReadingData.lastReadChapterTitle.split(" ")[0]}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.W600
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
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
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirmation
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
                onClick = onDismissRequest
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