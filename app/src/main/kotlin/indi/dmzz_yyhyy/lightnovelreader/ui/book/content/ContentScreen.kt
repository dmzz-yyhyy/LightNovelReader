package indi.dmzz_yyhyy.lightnovelreader.ui.book.content

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import kotlinx.coroutines.launch

@Composable
fun ContentScreen(
    onClickBackButton: () -> Unit,
    topBar: (@Composable () -> Unit) -> Unit,
    bottomBar: (@Composable () -> Unit) -> Unit,
    bookId: Int,
    chapterId: Int,
    viewModel: ContentViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val contentLazyColumnState = rememberLazyListState()
    var lastChapterId by remember { mutableStateOf(0) }
    var changed by remember { mutableStateOf(false) }
    var isImmersive by remember { mutableStateOf(false) }
    var readingChapterProgress by remember { mutableStateOf(0.0f) }
    if (lastChapterId != chapterId) {
        lastChapterId = chapterId
        viewModel.init(bookId, chapterId)
        changed = true
    }
    topBar {
        AnimatedVisibility(
            visible = !isImmersive,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            TopBar(onClickBackButton, viewModel.uiState.chapterContent.title)
        }
    }
    bottomBar {
        AnimatedVisibility(
            visible = !isImmersive,
            enter = expandVertically(expandFrom= Alignment.Top),
            exit = shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
                contentLazyColumnState.layoutInfo.visibleItemsInfo.firstOrNull()?.let {
                    readingChapterProgress = contentLazyColumnState.firstVisibleItemScrollOffset.toFloat() /
                            (it.size - contentLazyColumnState.layoutInfo.viewportSize.height)
                }
            BottomBar(
                chapterContent = viewModel.uiState.chapterContent,
                readingChapterProgress = readingChapterProgress,
                onClickLastChapter = {
                    viewModel.lastChapter()
                    coroutineScope.launch {
                        contentLazyColumnState.scrollToItem(0, 0)
                    }
                },
                onClickNextChapter = {
                    viewModel.nextChapter()
                    coroutineScope.launch {
                        contentLazyColumnState.scrollToItem(0, 0)
                    }
                }
            )
        }
    }
    AnimatedVisibility(
        visible =  viewModel.uiState.isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Loading()
    }
    AnimatedVisibility(
        visible = !viewModel.uiState.isLoading ,
        enter = fadeIn() + scaleIn(initialScale = 0.7f),
        exit = fadeOut() + scaleOut(targetScale = 0.7f)
    ) {
        AnimatedContent(viewModel.uiState.chapterContent.content, label = "ContentAnimate") {
            LazyColumn(
                modifier = Modifier
                    .animateContentSize()
                    .fillMaxSize()
                    .clickable(
                        interactionSource =
                        remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isImmersive = !isImmersive
                    },
                state = contentLazyColumnState
            ) {
                item {
                    Text(
                        modifier = Modifier.padding(18.dp, 8.dp),
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.W400
                    )
                }
            }
        }
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
                Icon(painterResource(id = R.drawable.arrow_back_24px), "back")
            }
        },
        title = {
            LazyRow {
                item {
                    AnimatedContent(title, label = "TitleAnimate") {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.W400,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        },
        actions = {
            IconButton(
                onClick = {}) {
                Icon(painterResource(id = R.drawable.menu_24px), "menu")
            }
        }
    )
}

@Composable
private fun BottomBar(
    chapterContent: ChapterContent,
    readingChapterProgress: Float,
    onClickLastChapter: () -> Unit,
    onClickNextChapter: () -> Unit
) {
    BottomAppBar {
        Box(Modifier.fillMaxHeight().width(12.dp))
        IconButton(
            onClick = onClickLastChapter,
            enabled = chapterContent.hasLastChapter()
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_back_24px),
                contentDescription = "lastChapter")
        }
        IconButton(
            onClick = {
                //TODO 添加至书签
            }) {
            Icon(
                painter = painterResource(R.drawable.outline_bookmark_24px),
                contentDescription = "mark")
        }
        IconButton(
            onClick = {
                //TODO 全屏
            }) {
            Icon(
                painter = painterResource(R.drawable.fullscreen_24px),
                contentDescription = "fullscreen")
        }
        IconButton(
            onClick = {
                //TODO 设置
            }) {
            Icon(
                painter = painterResource(R.drawable.outline_settings_24px),
                contentDescription = "setting")
        }
        Box(Modifier.padding(9.dp, 12.dp).weight(2f)) {
            Box(Modifier.clip(ButtonDefaults.shape)) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(24.dp, 11.5.dp)
                ) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "${(readingChapterProgress * 100).toInt()}% / 100%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.W500
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
        IconButton(
            onClick = onClickNextChapter,
            enabled = chapterContent.hasNextChapter()
        ) {
            Icon(
                painter = painterResource(R.drawable.arrow_forward_24px),
                contentDescription = "nextChapter")
        }
        Box(Modifier.fillMaxHeight().width(12.dp))
    }
}
