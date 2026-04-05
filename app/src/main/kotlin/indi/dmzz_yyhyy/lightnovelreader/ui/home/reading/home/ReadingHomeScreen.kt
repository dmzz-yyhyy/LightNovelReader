package indi.dmzz_yyhyy.lightnovelreader.ui.home.reading.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.unclippedBoundsInWindow
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ElasticPressContainer
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SectionHeader
import indi.dmzz_yyhyy.lightnovelreader.ui.components.rememberSkeletonShimmer
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.bottomBarPadding
import indi.dmzz_yyhyy.lightnovelreader.utils.bottomBarSpacer
import indi.dmzz_yyhyy.lightnovelreader.utils.formReadingDuration
import indi.dmzz_yyhyy.lightnovelreader.utils.formTime
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import indi.dmzz_yyhyy.lightnovelreader.utils.removeFromBookshelfAction
import indi.dmzz_yyhyy.lightnovelreader.utils.showSnackbar
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.UserReadingData
import kotlinx.coroutines.delay
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ReadingScreen(
    updateReadingBooks: () -> Unit,
    recentReadingBookInformationMap: Map<String, BookInformation>,
    recentReadingUserReadingDataMap: Map<String, UserReadingData>,
    recentReadingBookIds: List<String>,
    onClickBook: (String) -> Unit,
    onClickContinueReading: (String, String) -> Unit,
    onClickDownloadManager: () -> Unit,
    onClickStats: () -> Unit,
    onRemoveBook: (String) -> Unit,
    onAddBook: (String) -> Unit,
    @Suppress("unused") sharedTransitionScope: SharedTransitionScope,
    loadBookInfo: (String) -> Unit,
    onClickOpenChapters: (String) -> Unit,
) {
    LifecycleEventEffect(Lifecycle.Event.ON_CREATE) {
        updateReadingBooks()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopBar(
            onClickDownloadManager = onClickDownloadManager,
            onClickStats = onClickStats
        )

        if (recentReadingBookIds.isEmpty()) {
            EmptyPage(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .bottomBarPadding(),
                icon = painterResource(R.drawable.empty_90dp),
                title = stringResource(R.string.nothing_here),
                description = stringResource(R.string.nothing_here_desc_reading),
            )
        } else {
            ReadingContent(
                modifier = Modifier
                    .fillMaxSize(),
                onClickBook = onClickBook,
                onClickContinueReading = onClickContinueReading,
                onClickOpenChapters = onClickOpenChapters,
                onAddBook = onAddBook,
                onRemoveBook = onRemoveBook,
                recentReadingBookInformationMap = recentReadingBookInformationMap,
                recentReadingUserReadingDataMap = recentReadingUserReadingDataMap,
                recentReadingBookIds = recentReadingBookIds,
                loadBookInfo = loadBookInfo
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun ReadingContent(
    modifier: Modifier,
    onClickBook: (String) -> Unit,
    onClickContinueReading: (String, String) -> Unit,
    onAddBook: (String) -> Unit,
    onRemoveBook: (String) -> Unit,
    recentReadingBookInformationMap: Map<String, BookInformation>,
    recentReadingUserReadingDataMap: Map<String, UserReadingData>,
    recentReadingBookIds: List<String>,
    loadBookInfo: (String) -> Unit,
    onClickOpenChapters: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHost.current
    val listState = rememberLazyListState()

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1000)
        started = true
    }

    val shimmerInstance = rememberSkeletonShimmer(
        baseColor = colorScheme.surfaceContainerLow,
        highlightColor = colorScheme.surfaceContainerHigh
    )

    val headerItems by remember(recentReadingBookIds) {
        derivedStateOf {
            recentReadingBookIds.take(3).mapNotNull { id ->
                val info = recentReadingBookInformationMap[id]
                val user = recentReadingUserReadingDataMap[id]
                if (info != null && user != null && !info.isEmpty()) info to user else null
            }
        }
    }
    val removedItemString = stringResource(R.string.removed_item)
    val undoString = stringResource(R.string.undo)

    val deleteAction = remember(
        context,
        onRemoveBook,
        onAddBook
    ) {
        { bookId: String, bookTitle: String ->
            removeFromBookshelfAction.toSwipeAction {
                onRemoveBook(bookId)
                showSnackbar(
                    coroutineScope = coroutineScope,
                    hostState = snackbarHostState,
                    message = removedItemString + bookTitle,
                    actionLabel = undoString,
                ) {
                    when (it) {
                        SnackbarResult.Dismissed -> { }
                        SnackbarResult.ActionPerformed -> onAddBook(bookId)
                    }
                }
            }
        }
    }

    val density = LocalDensity.current
    val lineHeight = typography.titleMedium.lineHeight
    val headerLineHeight = typography.displayMedium.lineHeight

    val (titleHeight, headerTitleHeight) = remember(density) {
        with(density) {
            (lineHeight * 2).toDp() to (headerLineHeight * 2.2f).toDp()
        }
    }

    LazyColumn(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                val position = layoutCoordinates.unclippedBoundsInWindow()
                shimmerInstance.updateBounds(position)
            },
        verticalArrangement = Arrangement.spacedBy(12.dp),
        state = listState
    ) {
        if (
            recentReadingBookIds.isNotEmpty()
            && recentReadingUserReadingDataMap[recentReadingBookIds.first()] != null
            && recentReadingBookInformationMap[recentReadingBookIds.first()] != null
        ) {
            if (headerItems.isNotEmpty()) {
                item {
                    SectionHeader(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 14.dp),
                        text = stringResource(R.string.continue_reading)
                    )
                    ReadingHeaderCardPager(
                        items = headerItems,
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 8.dp),
                        onClickContinueReading = onClickContinueReading,
                        onClickOpenChapters = onClickOpenChapters,
                        titleHeight = headerTitleHeight
                    )
                }
            }
        }
        if (recentReadingBookIds.isNotEmpty()) {
            item {
                SectionHeader(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(
                        R.string.recent_reads, recentReadingBookIds.size,
                    )
                )
            }
        }
        items(
            items = recentReadingBookIds,
            key = { it },
            contentType = { "ReadingBookCard" }
        ) { id ->
            val info = recentReadingBookInformationMap[id]
            val userData = recentReadingUserReadingDataMap[id]

            LaunchedEffect(id) {
                loadBookInfo(id)
            }

            Box(
                modifier = Modifier
                    .animateItem()
                    .padding(horizontal = 12.dp)
                    .height(146.dp)
            ) {
                if (info != null && userData != null && info.isNotEmpty()) {
                    ReadingBookCard(
                        bookInformation = info,
                        userReadingData = userData,
                        onClick = { onClickBook(info.id) },
                        swipeToLeftActions = remember(id, info.title) {
                            listOf(deleteAction(id, info.title))
                        },
                        modifier = Modifier.fillMaxSize(),
                        titleHeight = titleHeight
                    )
                } else {
                    ReadingBookCardSkeleton(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(if (started) Modifier.shimmer(shimmerInstance) else Modifier)
                    )
                }
            }
        }
        navigationBarSpacer()
        bottomBarSpacer()
    }
}

@Composable
fun ReadingBookCardSkeleton(
    modifier: Modifier = Modifier
) {
    val roundedSmall = RoundedCornerShape(4.dp)
    val roundedLarge = RoundedCornerShape(8.dp)
    val baseColor = colorScheme.surfaceContainerLow

    Row(
        modifier = modifier
            .height(144.dp)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(94.dp, 142.dp)
                .clip(roundedLarge)
                .background(baseColor)
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 12.dp)
                .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(40.dp)
                    .clip(roundedSmall)
                    .background(baseColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.43f)
                    .height(20.dp)
                    .clip(roundedSmall)
                    .background(baseColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(roundedSmall)
                    .background(baseColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(roundedSmall)
                    .background(baseColor)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickDownloadManager: () -> Unit,
    onClickStats: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.nav_reading),
                style = typography.displayLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        actions = {
            IconButton(onClick = onClickDownloadManager) {
                Icon(
                    painter = painterResource(R.drawable.download_24px), null
                )
            }
            IconButton(
                onClick = onClickStats
            ) {
                Icon(
                    painter = painterResource(R.drawable.analytics_24px),
                    contentDescription = "statistics"
                )
            }
        }
    )
}

@Composable
private fun ReadingBookCard(
    modifier: Modifier = Modifier,
    titleHeight: Dp?,
    bookInformation: BookInformation,
    userReadingData: UserReadingData,
    onClick: () -> Unit,
    swipeToRightActions: List<SwipeAction> = emptyList(),
    swipeToLeftActions: List<SwipeAction> = emptyList(),
) {
    val lastRead = remember(userReadingData.lastReadTime) { formTime(userReadingData.lastReadTime) }
    val minutes = remember(userReadingData.totalReadTime) { formReadingDuration(userReadingData.totalReadTime) }
    val progress = remember(userReadingData.readingProgress) { "${(userReadingData.readingProgress * 100).toInt()}%" }
    val infoText = "$lastRead • $minutes • $progress"
    val description = remember(bookInformation.description) { bookInformation.description.trim() }

    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp))
    ) {
        SwipeableActionsBox(
            startActions = swipeToRightActions,
            endActions = swipeToLeftActions
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.surface)
                    .combinedClickable(onClick = onClick)
                    .padding(4.dp)
            ) {
                Cover(
                    width = 94.dp,
                    height = 144.dp,
                    uri = bookInformation.coverUri,
                    rounded = 8.dp,
                )

                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .fillMaxHeight()
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = if (titleHeight != null) Modifier
                            .height(titleHeight)
                            .wrapContentHeight(Alignment.CenterVertically)
                        else Modifier,
                        text = bookInformation.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = typography.titleMedium
                    )

                    Text(
                        text = bookInformation.author,
                        style = typography.bodyMedium.copy(
                            fontWeight = FontWeight.W600,
                            color = colorScheme.primary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = description,
                        style = typography.bodyMedium.copy(color = colorScheme.secondary),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_schedule_24px),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = colorScheme.secondary
                        )
                        Text(
                            text = infoText,
                            style = typography.labelMedium.copy(color = colorScheme.secondary)
                        )
                    }

                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                        progress = { userReadingData.readingProgress }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReadingHeaderCardPager(
    items: List<Pair<BookInformation, UserReadingData>>,
    modifier: Modifier = Modifier,
    onClickContinueReading: (String, String) -> Unit,
    onClickOpenChapters: (String) -> Unit,
    titleHeight: Dp?
) {
    val pageCount = items.size.coerceIn(1, 3)
    val pagerState = rememberPagerState(initialPage = 0) { pageCount }

    val showIndicator = remember { mutableStateOf(false) }
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) {
            showIndicator.value = true
        } else {
            delay(1800)
            showIndicator.value = false
        }
    }

    val isolatePager = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource) = Offset.Zero

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ) = Offset(0f, available.y)

            override suspend fun onPostFling(consumed: Velocity, available: Velocity) = available
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surface)
            .nestedScroll(isolatePager)
    ) {
        VerticalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
        ) { page ->
            val (book, user) = items[page]

            ElasticPressContainer(
                pagerState = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.surfaceVariant.copy(alpha = 0.14f))
                    .padding(8.dp)
            ) {
                ReadingHeaderCardPage(
                    info = book,
                    data = user,
                    onClickContinueReading = onClickContinueReading,
                    onClickOpenDetail = onClickOpenChapters,
                    modifier = Modifier.matchParentSize(),
                    titleHeight = titleHeight
                )
            }
        }

        AnimatedVisibility(
            modifier = Modifier.align(Alignment.CenterEnd),
            visible = showIndicator.value,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            VerticalDotsIndicator(
                pageCount = pageCount,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .padding(end = 6.dp)
            )
        }
    }
}

@Composable
private fun VerticalDotsIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(pageCount) { idx ->
            val selected = idx == currentPage

            val color by animateColorAsState(
                targetValue = if (selected) colorScheme.primary else colorScheme.outlineVariant,
                animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
            )

            Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun ReadingHeaderCardPage(
    info: BookInformation,
    data: UserReadingData,
    onClickContinueReading: (String, String) -> Unit,
    onClickOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    titleHeight: Dp?
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MM/dd") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    val dateText = data.lastReadTime.format(dateFormatter)
    val timeText = data.lastReadTime.format(timeFormatter)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cover(
            height = 172.dp,
            width = 118.dp,
            uri = info.coverUri,
            rounded = 8.dp
        )

        Column(
            modifier = Modifier
                .padding(start = 14.dp)
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChip(text = dateText, leadingPainter = painterResource(R.drawable.calendar_today_24px))
                InfoChip(text = timeText, leadingPainter = painterResource(R.drawable.schedule_90dp))
            }

            Text(
                text = info.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.W700,
                style = typography.displayMedium,
                modifier = if (titleHeight != null) Modifier
                    .height(titleHeight)
                    .wrapContentHeight(Alignment.CenterVertically)
                else Modifier
            )

            Text(
                text = data.lastReadChapterTitle,
                maxLines = 1,
                style = typography.labelLarge,
                color = colorScheme.tertiary,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.W600
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FilledTonalButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onClickOpenDetail(info.id) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.view_list_24px),
                        contentDescription = null
                    )
                }

                Button(
                    modifier = Modifier.weight(3f),
                    onClick = { onClickContinueReading(info.id, data.lastReadChapterId) },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.resume_last_reading),
                        fontWeight = FontWeight.W600,
                        style = typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

    }
}


@Composable
private fun InfoChip(
    modifier: Modifier = Modifier,
    text: String,
    leadingPainter: Painter? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colorScheme.surfaceVariant.copy(alpha = 0.9f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingPainter != null) {
            Icon(
                painter = leadingPainter,
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(14.dp),
                tint = colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = text,
            style = typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}
