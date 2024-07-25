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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import java.text.DecimalFormat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentScreen(
    onClickBackButton: () -> Unit,
    topBar: (@Composable () -> Unit) -> Unit,
    bottomBar: (@Composable () -> Unit) -> Unit,
    bookId: Int,
    chapterId: Int,
    viewModel: ContentViewModel = hiltViewModel()
) {
    val currentView = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val contentLazyColumnState = rememberLazyListState()
    val bottomSheetState = rememberModalBottomSheetState()
    var lastChapterId by remember { mutableStateOf(0) }
    var changed by remember { mutableStateOf(false) }
    var isImmersive by remember { mutableStateOf(false) }
    var readingChapterProgress by remember { mutableStateOf(0.0f) }
    var showBottomSheet by remember { mutableStateOf(false) }

    var fontSize by remember { mutableStateOf(16.0f) }
    var fontLineHeight by remember { mutableStateOf(0.0f) }
    var keepScreenOn by remember { mutableStateOf(false) }

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
                readingChapterProgress = contentLazyColumnState.firstVisibleItemScrollOffset.toFloat() /
                        (contentLazyColumnState.layoutInfo.visibleItemsInfo.sumOf { it.size } - contentLazyColumnState.layoutInfo.viewportSize.height)
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
                },
                onClickSettings = { showBottomSheet = true }
            )
        }
    }
    DisposableEffect(Unit) {
        currentView.keepScreenOn = keepScreenOn
        onDispose {
            currentView.keepScreenOn = false
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
            ContentTextComponent(
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
                state = contentLazyColumnState,
                content = it,
                fontSize = fontSize,
                fontLineHeight = fontLineHeight
            )
        }
    }
    AnimatedVisibility(visible = showBottomSheet) {
        SettingsBottomSheet(
            state = bottomSheetState,
            onDismissRequest = {
                coroutineScope.launch { bottomSheetState.hide() }.invokeOnCompletion {
                    if (!bottomSheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
                showBottomSheet = false
            },
            fontSize = fontSize,
            onFontSizeSliderChange = {
                fontSize = it
            },
            onFontSizeSliderChangeFinished = {
            },
            fontLineHeight = fontLineHeight,
            onFontLineHeightSliderChange = {
                fontLineHeight = it
            },
            onFontLineHeightSliderChangeFinished = {
            },
            isKeepScreenOn = keepScreenOn,
            onKeepScreenOnChange = {
                keepScreenOn = it
            }
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
    onClickNextChapter: () -> Unit,
    onClickSettings: () -> Unit
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
            onClick = onClickSettings) {
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

@Composable
fun ContentTextComponent(
    modifier: Modifier,
    state: LazyListState,
    content: String,
    fontSize: Float,
    fontLineHeight: Float,
) {
    LazyColumn(
        modifier = modifier,
        state = state,
    ) {
        items(
            content
            .replace("[image]", "ImageSplitMark[image]")
            .replace("[/image]", "ImageSplitMark")
            .split("ImageSplitMark")
        ) {
            if (it.startsWith("[image]"))
                AsyncImage(
                    modifier = Modifier.fillMaxWidth(),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(it
                            .replace("[image]", "")
                            .replace("\n", "")
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = null
                )
            else Text(
                modifier = Modifier.padding(18.dp, 8.dp),
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.W400,
                fontSize = fontSize.sp,
                lineHeight = (fontSize + fontLineHeight).sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    state: SheetState,
    onDismissRequest: () -> Unit,
    fontSize: Float,
    onFontSizeSliderChange: (Float) -> Unit,
    onFontSizeSliderChangeFinished: () -> Unit,
    fontLineHeight: Float,
    onFontLineHeightSliderChange: (Float) -> Unit,
    onFontLineHeightSliderChangeFinished: () -> Unit,
    isKeepScreenOn: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = state
    ) {
        Box(
            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 22.dp)
        ) {
            Column(
                modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsSlider(
                    describe = "阅读器字体大小",
                    unit = "sp",
                    value = fontSize,
                    valueRange = 8f..64f,
                    onSlideChange = onFontSizeSliderChange,
                    onSliderChangeFinished = onFontSizeSliderChangeFinished
                )
                SettingsSlider(
                    describe = "阅读器字距大小",
                    unit = "sp",
                    valueRange = 0f..32f,
                    value = fontLineHeight,
                    onSlideChange = onFontLineHeightSliderChange,
                    onSliderChangeFinished = onFontLineHeightSliderChangeFinished
                )
                SettingsSwitch(
                    title = "屏幕常亮",
                    describe = "在阅读页时，总是保持屏幕开启。这将导致耗电量增加",
                    checked = isKeepScreenOn,
                    onCheckedChange = onKeepScreenOnChange,
                )
            }
        }
    }
}

@Composable
fun SettingsSlider(
    describe: String,
    unit: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onSlideChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit
) {
    FilledCard {
        Column(Modifier.padding(21.dp, 19.dp, 21.dp, 9.dp)) {
            Text(
                text = describe,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.W500,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = "${DecimalFormat("#.#").format(value)}$unit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.W500,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1
            )
            Slider(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                valueRange = valueRange,
                onValueChange = onSlideChange,
                onValueChangeFinished = onSliderChangeFinished,
                colors = SliderDefaults.colors(
                    inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer,
                ),
                steps = ((valueRange.endInclusive - valueRange.start)*2-1).toInt()
            )
        }
    }
}

@Composable
fun SettingsSwitch(
    title: String,
    describe: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    FilledCard(Modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().padding(21.dp, 10.dp, 19.dp, 12.dp)) {
            Column (Modifier.align(Alignment.CenterStart)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W500,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = describe,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.W500,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Switch(
                modifier = Modifier.align(Alignment.CenterEnd),
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun FilledCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier,
        content = content
    )
}