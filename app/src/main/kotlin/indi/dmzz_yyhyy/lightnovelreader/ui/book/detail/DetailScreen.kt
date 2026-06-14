package indi.dmzz_yyhyy.lightnovelreader.ui.book.detail

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.get
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadItem
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.LnrSnackbar
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SwitchChip
import indi.dmzz_yyhyy.lightnovelreader.ui.components.rememberSkeletonShimmer
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home.BookStatusIcon
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.textformatting.rules.navigateToSettingsTextFormattingRulesDestination
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalClaimSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.dateFormatter
import indi.dmzz_yyhyy.lightnovelreader.utils.fadeInOnce
import indi.dmzz_yyhyy.lightnovelreader.utils.fadingEdge
import indi.dmzz_yyhyy.lightnovelreader.utils.isScrollingUp
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.ui.LocalNavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    uiState: DetailUiState,
    onClickExportToEpub: (ExportSettings) -> Unit,
    onClickBackButton: () -> Unit,
    onClickChapter: (String) -> Unit,
    onClickReadFromStart: () -> Unit,
    onClickContinueReading: () -> Unit,
    cacheBook: (String) -> Unit,
    requestAddBookToBookshelf: (String) -> Unit,
    onClickTag: (String) -> Unit,
    onClickCover: (Uri) -> Unit,
    onClickMarkAsRead: () -> Unit
) {
    val navController = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = LocalSnackbarHost.current

    val exportBottomSheetState = rememberBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    val infoBottomSheetState = rememberBottomSheetState(initialValue = SheetValue.PartiallyExpanded)

    var showExportBottomSheet by remember { mutableStateOf(false) }
    var showInfoBottomSheet by remember { mutableStateOf(false) }
    var exportSettings by remember { mutableStateOf(ExportSettings()) }

    val lazyListState = rememberLazyListState()
    val volumesEmpty = uiState.bookVolumes.volumes.isEmpty()

    val isCollapsed by remember {
        derivedStateOf {
            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) false
            else visibleItems.none { it.index == 0 }
        }
    }

    val claim = LocalClaimSnackbarHost.current

    DisposableEffect(Unit) {
        claim(true)
        onDispose { claim(false) }
    }

    val scrollingUp by lazyListState.isScrollingUp()
    val fabVisible by remember(uiState.bookVolumes.volumes, lazyListState) {
        derivedStateOf {
            val hasVolumes = uiState.bookVolumes.volumes.isNotEmpty()
            val allowByDirection = !lazyListState.isScrollInProgress || scrollingUp
            val canGoForward = lazyListState.canScrollForward

            hasVolumes && canGoForward && allowByDirection
        }
    }


    val isStartReading = uiState.userReadingData.lastReadChapterId.isBlank()
    val fabTextRes = if (isStartReading) R.string.start_reading else R.string.continue_reading

    val fabContent = remember {
        movableContentOf<Boolean, Int, () -> Unit> { visible, textRes, onClick ->
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(200, easing = FastOutSlowInEasing)),
                exit = slideOutVertically(
                    targetOffsetY = { it / 4 },
                    animationSpec = tween(200, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(150, easing = FastOutSlowInEasing))
            ) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(end = 28.dp, bottom = 28.dp),
                    onClick = onClick,
                    icon = { Icon(painterResource(R.drawable.filled_menu_book_24px), null) },
                    text = { Text(stringResource(textRes)) }
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp)
            ) {
                val targetBottomPad = if (fabVisible) 90.dp else 32.dp
                val snackbarBottomPad by animateDpAsState(
                    targetValue = targetBottomPad,
                    animationSpec = tween(250, easing = FastOutSlowInEasing),
                    label = "snackbarBottomPad"
                )

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = snackbarBottomPad)
                ) { data ->
                    LnrSnackbar(data)
                }

                Box(modifier = Modifier.align(Alignment.BottomEnd)) {
                    fabContent(fabVisible, fabTextRes) {
                        if (isStartReading) onClickReadFromStart() else onClickContinueReading()
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TopBar(
                title = uiState.bookInformation.title,
                readingProgress = uiState.userReadingData.readingProgress,
                volumesEmpty = volumesEmpty,
                onClickBackButton = onClickBackButton,
                onClickExport = { showExportBottomSheet = true },
                onClickTextFormatting = {
                    navController.navigateToSettingsTextFormattingRulesDestination(
                        uiState.bookInformation.id
                    )
                },
                onClickMarkAsRead = onClickMarkAsRead,
                scrollBehavior = scrollBehavior,
                isCollapsed = isCollapsed
            )

            Crossfade(
                targetState = uiState.isLoading || uiState.bookInformation.title.isEmpty(),
                animationSpec = tween(300),
                label = "DetailScreenCrossfade"
            ) { empty ->
                if (empty) {
                    DetailContentSkeleton(
                        Modifier
                            .fillMaxSize()
                            .background(colorScheme.surface)
                    )
                } else {
                    DetailContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorScheme.surface),
                        uiState = uiState,
                        onClickChapter = onClickChapter,
                        lazyListState = lazyListState,
                        cacheBook = cacheBook,
                        requestAddBookToBookshelf = requestAddBookToBookshelf,
                        onClickTag = onClickTag,
                        onClickCover = onClickCover,
                        onClickShowInfo = { showInfoBottomSheet = true }
                    )
                }
            }
        }

        if (showExportBottomSheet) {
            ExportBottomSheet(
                sheetState = exportBottomSheetState,
                bookVolumes = uiState.bookVolumes,
                settings = exportSettings,
                onSettingsChange = { exportSettings = it },
                onDismissRequest = { showExportBottomSheet = false },
                onClickExport = onClickExportToEpub
            )
        }
        AnimatedVisibility(visible = showInfoBottomSheet) {
            BookInfoBottomSheet(
                bookInformation = uiState.bookInformation,
                bookVolumes = uiState.bookVolumes,
                sheetState = infoBottomSheetState,
                onDismissRequest = { showInfoBottomSheet = false }
            )
        }

    }
}


@Composable
private fun DetailContentSkeleton(modifier: Modifier = Modifier) {
    val rounded = RoundedCornerShape(6.dp)
    var started by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        started = true
    }

    val baseColor = colorScheme.surfaceContainerLow
    val highlightColor = colorScheme.surfaceContainerHigh

    val shimmer = rememberSkeletonShimmer(
        baseColor, highlightColor
    )

    Column(
        modifier = modifier
            .then(if (started) Modifier.shimmer(shimmer) else Modifier),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(188.dp)
                .padding(horizontal = itemHorizontalPadding, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 122.dp, height = 178.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(baseColor)
            )
            Column(
                modifier = Modifier.padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(20.dp)
                            .clip(rounded)
                            .background(baseColor)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(baseColor)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp)
                        .padding(vertical = itemVerticalPadding)
                        .clip(RoundedCornerShape(8.dp))
                        .background(baseColor)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(24.dp)
                    .clip(rounded)
                    .background(baseColor)
            )
            Spacer(Modifier.height(10.dp))
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(rounded)
                        .background(baseColor)
                )
            }
        }
    }
}


private val itemHorizontalPadding = 18.dp
private val itemVerticalPadding = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailContent(
    modifier: Modifier = Modifier,
    uiState: DetailUiState,
    lazyListState: LazyListState,
    onClickChapter: (String) -> Unit,
    cacheBook: (String) -> Unit,
    requestAddBookToBookshelf: (String) -> Unit,
    onClickTag: (String) -> Unit,
    onClickCover: (Uri) -> Unit,
    onClickShowInfo: () -> Unit
) {
    var hideReadChapters by remember { mutableStateOf(false) }
    val deferred = 6
    val framesPerStep = 2
    var visible by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (visible < deferred) {
            repeat(framesPerStep) { withFrameNanos { } }
            visible += 1
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier
    ) {
        if (visible >= 1) item {
            BookCardBlock(
                bookInformation = uiState.bookInformation,
                modifier = Modifier
                    .fadeInOnce("book")
                    .graphicsLayer {
                        translationY = lazyListState.firstVisibleItemScrollOffset * 0.5f
                    }
                    .fillMaxWidth(),
                onClickCover = onClickCover
            )
        }

        if (visible >= 2) item {
            TagsBlock(
                modifier = Modifier.fadeInOnce("tags"),
                bookInformation = uiState.bookInformation,
                onClickTag = onClickTag
            )
        }

        if (visible >= 3) item {
            QuickOperationsBlock(
                modifier = Modifier.fadeInOnce("op"),
                isInBookshelf = uiState.isInBookshelf,
                isCached = uiState.isCached,
                downloadItem = uiState.downloadItem,
                onClickAddToBookShelf = { requestAddBookToBookshelf(uiState.bookInformation.id) },
                onClickCache = { cacheBook(uiState.bookInformation.id) },
                onClickShowInfo = onClickShowInfo
            )
        }

        if (visible >= 4) item {
            IntroBlock(
                modifier = Modifier.fadeInOnce("intro"),
                description = uiState.bookInformation.description
            )
        }

        if (visible >= 5) item {
            Row(
                modifier = Modifier
                    .fadeInOnce("contents")
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.detail_contents),
                    style = typography.displayMedium,
                    fontWeight = FontWeight.W600
                )
                Spacer(Modifier.width(12.dp))
                SwitchChip(
                    label = stringResource(R.string.hide_read),
                    selected = hideReadChapters,
                    onClick = { hideReadChapters = !hideReadChapters }
                )
            }
        }

        if (visible >= 6) item {
            AnimatedVisibility(
                modifier = Modifier.fadeInOnce("loading"),
                visible = uiState.bookVolumes.volumes.isEmpty(),
                enter = fadeIn(tween(300, 1000)),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Loading()
                }
            }
        }

        if (visible >= 6) items(
            items = uiState.bookVolumes.volumes,
            key = { it.volumeId }
        ) { volume ->
            VolumeItem(
                modifier = Modifier.fadeInOnce(volume.volumeId),
                volume = volume,
                hideReadChapters = hideReadChapters,
                readCompletedChapterIds = uiState.userReadingData.currentChapterReadingProgressMap.filterValues { it >= 1f }.keys.toList(),
                onClickChapter = onClickChapter,
                volumesSize = uiState.bookVolumes.volumes.size,
                lastReadingChapterId = uiState.userReadingData.lastReadChapterId
            )
        }

        item {
            Spacer(Modifier.height(48.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    readingProgress: Float,
    volumesEmpty: Boolean,
    onClickBackButton: () -> Unit,
    onClickExport: () -> Unit,
    onClickTextFormatting: () -> Unit,
    onClickMarkAsRead: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    isCollapsed: Boolean
) {
    val titleProgress by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "titleProgress"
    )

    val barAlpha by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "barAlpha"
    )

    val barProgress by animateFloatAsState(
        targetValue = readingProgress.coerceIn(0f, 1f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "barProgress"
    )

    Box {
        TopAppBar(
            title = {
                val offset = 16.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = stringResource(R.string.detail_title),
                        maxLines = 1,
                        style = typography.displayLarge,
                        modifier = Modifier
                            .offset(y = (-offset * titleProgress))
                            .graphicsLayer { alpha = 1f - titleProgress }
                    )
                    Text(
                        text = title,
                        maxLines = 1,
                        style = typography.displayLarge,
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .offset(y = (offset * (1f - titleProgress)))
                            .graphicsLayer { alpha = titleProgress }
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onClickBackButton) {
                    Icon(painterResource(id = R.drawable.arrow_back_24px), contentDescription = "back")
                }
            },
            actions = {
                TopBarActions(
                    volumesEmpty = volumesEmpty,
                    onClickExport = onClickExport,
                    onClickTextFormatting = onClickTextFormatting,
                    onClickMarkAsRead = onClickMarkAsRead
                )
            },
            scrollBehavior = scrollBehavior
        )

        Box(
            Modifier
                .matchParentSize()
        ) {
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .alpha(barAlpha)
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(colorScheme.surfaceVariant)
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(barProgress)
                        .background(colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun TopBarActions(
    volumesEmpty: Boolean,
    onClickExport: () -> Unit,
    onClickTextFormatting: () -> Unit,
    onClickMarkAsRead: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    IconButton(enabled = !volumesEmpty, onClick = onClickExport) {
        Icon(painterResource(id = R.drawable.file_export_24px), contentDescription = "export")
    }
    IconButton(enabled = !volumesEmpty, onClick = onClickTextFormatting) {
        Icon(painterResource(id = R.drawable.find_replace_24px), contentDescription = "formatting")
    }
    Box {
        IconButton(enabled = !volumesEmpty, onClick = { menuExpanded = true }) {
            Icon(painterResource(id = R.drawable.more_vert_24px), contentDescription = "more")
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.mark_as_read), style = typography.bodyLarge) },
                onClick = {
                    menuExpanded = false
                    onClickMarkAsRead()
                }
            )
        }
    }
}


@Composable
private fun BookCardBlock(
    bookInformation: BookInformation,
    modifier: Modifier,
    onClickCover: (Uri) -> Unit
) {
    val updateText = if (bookInformation.isComplete) {
        stringResource(R.string.book_completed)
    } else {
        stringResource(
            R.string.book_info_update_date,
            bookInformation.lastUpdated.format(dateFormatter())
        )
    }
    val wordCountText = bookInformation.wordCount.get()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(188.dp)
            .padding(horizontal = itemHorizontalPadding, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .clickable(
                    onClick = {
                        onClickCover(bookInformation.coverUri)
                    }
                )
        ) {
            Cover(
                height = 178.dp,
                width = 122.dp,
                uri = bookInformation.coverUri,
                rounded = 8.dp
            )
        }
        Column(
            modifier = Modifier
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = bookInformation.title,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.W600,
                style = typography.displayMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            if (bookInformation.subtitle.isNotEmpty()) {
                Text(
                    text = bookInformation.subtitle,
                    maxLines = 2,
                    color = colorScheme.secondary,
                    style = typography.bodyMedium
                )
            }
            Text(
                text = bookInformation.author,
                maxLines = 1,
                fontWeight = FontWeight.W600,
                color = colorScheme.primary,
                style = typography.bodyLarge
            )
            Column {
                InfoRow(
                    icon = { BookStatusIcon(bookInformation.isComplete) },
                    text = updateText
                )
                Spacer(Modifier.height(2.dp))
                InfoRow(
                    icon = {
                        Icon(
                            painter = painterResource(R.drawable.text_snippet_24px),
                            contentDescription = null,
                            tint = colorScheme.outline,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                    },
                    text = wordCountText
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: @Composable () -> Unit,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Text(
            text = text,
            maxLines = 1,
            style = typography.labelMedium,
            color = colorScheme.secondary
        )
    }
}


@Composable
private fun TagsBlock(
    modifier: Modifier,
    bookInformation: BookInformation,
    onClickTag: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.surface)
            .horizontalScroll(rememberScrollState())
            .padding(vertical = itemVerticalPadding, horizontal = itemHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (bookInformation.publishingHouse.isNotEmpty()) {
            SuggestionChip(
                label = { Text(bookInformation.publishingHouse) },
                onClick = {}
            )
        }

        bookInformation.tags.forEach { tag ->
            SuggestionChip(
                label = { Text(tag) },
                onClick = { onClickTag(tag) }
            )
        }
    }
}


@Composable
fun QuickOperationButton(
    icon: Painter,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        contentPadding = PaddingValues(12.dp),
        modifier = modifier
            .height(72.dp)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(0.dp),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = icon,
                contentDescription = null,
                tint = colorScheme.primary
            )
            Text(
                text = title,
                color = colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QuickOperationsBlock(
    modifier: Modifier,
    isInBookshelf: Boolean,
    isCached: Boolean,
    downloadItem: DownloadItem?,
    onClickAddToBookShelf: () -> Unit,
    onClickCache: () -> Unit,
    onClickShowInfo: () -> Unit
) {
    val bookmark = painterResource(R.drawable.bookmark_add_24px)
    val filledBookmark = painterResource(R.drawable.filled_bookmark_add_24px)
    val cloud = painterResource(R.drawable.cloud_download_24px)
    val filledCloud = painterResource(R.drawable.filled_cloud_download_24px)
    val info = painterResource(R.drawable.info_24px)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorScheme.surface)
            .padding(horizontal = itemHorizontalPadding, vertical = itemVerticalPadding)
            .clip(RoundedCornerShape(16.dp)),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isInBookshelf) {
            QuickOperationButton(
                icon = filledBookmark,
                title = stringResource(R.string.activity_collections),
                onClick = onClickAddToBookShelf,
                modifier = Modifier.weight(1f)
            )
        } else {
            QuickOperationButton(
                icon = bookmark,
                title = stringResource(R.string.add_to_bookshelf),
                onClick = onClickAddToBookShelf,
                modifier = Modifier.weight(1f)
            )
        }

        if (isCached) {
            QuickOperationButton(
                icon = filledCloud,
                title = if (downloadItem == null || downloadItem.progress == 1f)
                    stringResource(R.string.cached)
                else
                    "${(downloadItem.progress * 100).toInt()}%",
                onClick = { },
                modifier = Modifier.weight(1f)
            )
        } else {
            QuickOperationButton(
                icon = cloud,
                title = if (downloadItem == null)
                    stringResource(R.string.cached_false)
                else
                    "${(downloadItem.progress * 100).toInt()}%",
                onClick = if (downloadItem == null) onClickCache else { {} },
                modifier = Modifier.weight(1f)
            )
        }

        QuickOperationButton(
            icon = info,
            title = stringResource(R.string.action_show_info),
            onClick = onClickShowInfo,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun IntroBlock(
    modifier: Modifier,
    description: String
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val overflowed = remember(description) { description.length > 220 }

    val fadingBrush = remember {
        Brush.verticalGradient(
            0.7f to Color.White,
            1f to Color.Transparent
        )
    }
    val whiteBrush = remember {
        Brush.verticalGradient(listOf(Color.White, Color.White))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = itemHorizontalPadding, vertical = itemVerticalPadding),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            modifier = Modifier.padding(vertical = 16.dp),
            text = stringResource(R.string.detail_introduction),
            style = typography.displayMedium,
            fontWeight = FontWeight.W600
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .fadingEdge(
                    if (!expanded && overflowed) fadingBrush else whiteBrush
                )
        ) {
            Text(
                text = description,
                style = typography.bodyLarge,
                maxLines = if (!expanded && overflowed) 4 else Int.MAX_VALUE,
                color = colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (overflowed) {
            val rotation by animateFloatAsState(if (expanded) 0f else 180f)
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = { expanded = !expanded },
                colors = ButtonDefaults.textButtonColors(containerColor = Color.Transparent)
            ) {
                Icon(
                    painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation),
                    tint = colorScheme.primary
                )
                Text(
                    text = if (expanded)
                        stringResource(R.string.collapse)
                    else
                        stringResource(R.string.expand),
                    color = colorScheme.primary
                )
            }
        } else {
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun VolumeItem(
    modifier: Modifier,
    volume: Volume,
    hideReadChapters: Boolean = false,
    readCompletedChapterIds: List<String>,
    onClickChapter: (String) -> Unit,
    volumesSize: Int,
    lastReadingChapterId: String
) {
    val readIds = remember(readCompletedChapterIds) { readCompletedChapterIds.toSet() }
    val (readCount, totalCount) = remember(volume.volumeId, readIds) {
        val count = volume.chapters.count { it.id in readIds }
        count to volume.chapters.size
    }
    val isFullyRead = readCount >= totalCount
    var expanded by rememberSaveable {
        mutableStateOf(readCount < totalCount || volumesSize > 8)
    }
    val rotation by animateFloatAsState(targetValue = if (expanded) 90f else 0f, animationSpec = tween(200))

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier
                .weight(5f)
                .padding(vertical = 12.dp)
            ) {
                Text(
                    text = volume.volumeTitle,
                    style = typography.titleMedium,
                    color = if (isFullyRead) colorScheme.secondary
                    else colorScheme.onSurface
                )
                Text(
                    text = if (isFullyRead) stringResource(R.string.info_reading_finished)
                    else stringResource(R.string.info_reading_progress, readCount, totalCount),
                    style = typography.titleSmall,
                    fontWeight = FontWeight.Normal,
                    color = colorScheme.secondary
                )
            }
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(
                visible = !hideReadChapters || !isFullyRead,
                enter = fadeIn(animationSpec = tween(180)) +
                        slideInHorizontally(
                            animationSpec = tween(180),
                            initialOffsetX = { it / 4 }
                        ),
                exit = fadeOut(animationSpec = tween(140)) +
                        slideOutHorizontally(
                            animationSpec = tween(140),
                            targetOffsetX = { it / 4 }
                        )
            ) {
                Icon(
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(rotation),
                    painter = painterResource(id = R.drawable.arrow_forward_ios_24px),
                    contentDescription = null
                )
            }
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.animateContentSize(animationSpec = tween(250))) {
            if (expanded) {
                volume.chapters.forEach { chapter ->
                    val visible = !(hideReadChapters && chapter.id in readIds)
                    if (visible) {
                        ChapterItem(
                            chapter = chapter,
                            isRead = chapter.id in readIds,
                            isLastRead = chapter.id == lastReadingChapterId,
                            onClick = { onClickChapter(chapter.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: ChapterInformation,
    isRead: Boolean,
    isLastRead: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
            .padding(start = 32.dp, end = 27.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chapter.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = typography.titleMedium,
                    fontWeight = if (isRead) FontWeight.Normal else FontWeight.W600,
                    color = if (isRead) colorScheme.secondary
                    else colorScheme.onSurface
                )
                if (isLastRead) {
                    Text(
                        text = stringResource(R.string.last_read),
                        maxLines = 1,
                        style = typography.titleSmall,
                        fontWeight = FontWeight.Normal,
                        color = colorScheme.primary
                    )
                }
            }
            if (isLastRead)
                Icon(
                    modifier = Modifier
                        .padding(start = 22.dp)
                        .size(24.dp),
                    painter = painterResource(R.drawable.target_24px),
                    tint = colorScheme.primary,
                    contentDescription = "last_read"
                )

        }
    }
}
