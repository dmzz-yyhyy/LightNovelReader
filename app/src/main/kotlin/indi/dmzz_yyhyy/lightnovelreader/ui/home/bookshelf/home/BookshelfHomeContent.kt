package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.promeg.pinyinhelper.Pinyin
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.unclippedBoundsInWindow
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.BookCardItem
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.utils.bottomBarPadding
import indi.dmzz_yyhyy.lightnovelreader.utils.bottomBarSpacer
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfBookMetadata
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType
import kotlinx.coroutines.delay
import java.text.Collator
import java.time.LocalDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookshelfHomeContent(
    uiState: BookshelfHomeUiState,
    dataSources: BookshelfHomeDataSources,
    listState: LazyListState,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !uiState.selectMode,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            if (uiState.bookshelfList.isNotEmpty()) {
                val selectedIndex = uiState.selectedTabIndex
                    .takeIf { it in uiState.bookshelfList.indices } ?: 0

                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedIndex,
                    edgePadding = 0.dp,
                    indicator = {
                        SecondaryIndicator(
                            modifier = Modifier
                                .tabIndicatorOffset(
                                    selectedTabIndex = selectedIndex,
                                    matchContentSize = true
                                )
                                .height(4.dp)
                                .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                ) {
                    uiState.bookshelfList.forEach { bookshelf ->
                        Tab(
                            selected = uiState.selectedBookshelfId == bookshelf.id,
                            onClick = {
                                if (!uiState.selectMode) uiState.changePage(bookshelf.id)
                            },
                            text = {
                                Text(
                                    text = bookshelf.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }
        }

        val allBookIds = remember(uiState.selectedBookshelf.allBookIds) {
            uiState.selectedBookshelf.allBookIds.toList()
        }
        val bookInfoMap = linkedMapOf<String, BookInformation>()
        allBookIds.forEach { id ->
            val infoFlow = remember(id) { dataSources.getBookInfoFlow(id) }
            val info by infoFlow.collectAsStateWithLifecycle()
            bookInfoMap[id] = info
        }
        val bookMetadataMap = linkedMapOf<String, BookshelfBookMetadata?>()
        allBookIds.forEach { id ->
            val metadataFlow = remember(id) { dataSources.getBookMetadataFlow(id) }
            val metadata by metadataFlow.collectAsStateWithLifecycle()
            bookMetadataMap[id] = metadata
        }

        val updatedIds = sortBookIds(
            sourceIds = uiState.selectedBookshelf.updatedBookIds,
            allBookIds = allBookIds,
            sortType = uiState.selectedBookshelf.sortType,
            sortReversed = uiState.selectedBookshelf.sortReversed,
            bookInfoMap = bookInfoMap,
            bookMetadataMap = bookMetadataMap
        )
        val pinnedIds = sortBookIds(
            sourceIds = uiState.selectedBookshelf.pinnedBookIds,
            allBookIds = allBookIds,
            sortType = uiState.selectedBookshelf.sortType,
            sortReversed = uiState.selectedBookshelf.sortReversed,
            bookInfoMap = bookInfoMap,
            bookMetadataMap = bookMetadataMap
        )
        val visibleAllIds = sortBookIds(
            sourceIds = allBookIds,
            allBookIds = allBookIds,
            sortType = uiState.selectedBookshelf.sortType,
            sortReversed = uiState.selectedBookshelf.sortReversed,
            bookInfoMap = bookInfoMap,
            bookMetadataMap = bookMetadataMap
        )

        val selectedBookIdSet = uiState.selectedBookIds.toHashSet()
        val onLongPress: (String) -> Unit = { bookId ->
            if (!uiState.selectMode) {
                uiState.onEnableSelectMode()
            }
            uiState.changeBookSelectState(bookId)
        }
        var initialScrollApplied by remember(uiState.selectedBookshelfId) { mutableStateOf(false) }
        var showEmptyPage by remember { mutableStateOf(allBookIds.isEmpty()) }

        LaunchedEffect(uiState.selectedBookshelfId, allBookIds.isNotEmpty()) {
            if (initialScrollApplied || allBookIds.isEmpty()) return@LaunchedEffect
            listState.scrollToItem(0)
            initialScrollApplied = true
        }
        LaunchedEffect(allBookIds) {
            if (allBookIds.isEmpty()) {
                delay(140)
                showEmptyPage = true
            } else {
                showEmptyPage = false
            }
        }
        val shimmerInstance = rememberShimmer(ShimmerBounds.Custom)
        val density = LocalDensity.current
        val lineHeight = MaterialTheme.typography.titleMedium.lineHeight
        val titleHeight = with(density) { (lineHeight * 2.2f).toDp() }

        AnimatedVisibility(
            visible = showEmptyPage,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EmptyPage(
                modifier = Modifier
                    .navigationBarsPadding()
                    .bottomBarPadding(),
                icon = painterResource(R.drawable.bookmarks_90px),
                title = stringResource(R.string.nothing_here),
                description = stringResource(R.string.nothing_here_desc_bookshelf)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .onGloballyPositioned { layoutCoordinates ->
                    shimmerInstance.updateBounds(layoutCoordinates.unclippedBoundsInWindow())
                },
            state = listState
        ) {
            if (updatedIds.isNotEmpty()) {
                stickyHeader {
                    CollapseHeader(
                        icon = painterResource(R.drawable.autorenew_24px),
                        title = stringResource(R.string.bookshelf_group_title_updated, updatedIds.size),
                        expanded = uiState.updatedExpanded,
                        onToggleExpand = { uiState.updatedExpanded = !uiState.updatedExpanded }
                    )
                }
                if (uiState.updatedExpanded) {
                    items(updatedIds, key = { "updated_$it" }, contentType = { "book_card" }) { id ->
                        val info = bookInfoMap[id] ?: BookInformation.empty(id)
                        val volumesFlow = remember(id) { dataSources.getBookVolumesFlow(id) }
                        val volumes by volumesFlow.collectAsStateWithLifecycle()
                        val lastChapterTitle by remember(volumes) {
                            derivedStateOf {
                                if (volumes.volumes.isNotEmpty()) {
                                    "${volumes.volumes.last().volumeTitle} ${volumes.volumes.last().chapters.last().title}"
                                } else {
                                    null
                                }
                            }
                        }
                        BookshelfBookCard(
                            id = id,
                            info = info,
                            selected = selectedBookIdSet.contains(id),
                            selectMode = uiState.selectMode,
                            latestChapterTitle = lastChapterTitle ?: uiState.bookLastChapterTitleMap[id],
                            titleHeight = titleHeight,
                            shimmer = shimmerInstance,
                            onBookClick = uiState.onBookClick,
                            onBookSelect = uiState.changeBookSelectState,
                            onLongPress = onLongPress
                        )
                    }
                }
            }

            if (pinnedIds.isNotEmpty()) {
                stickyHeader {
                    CollapseHeader(
                        icon = painterResource(R.drawable.keep_24px),
                        title = stringResource(R.string.bookshelf_group_title_pinned, pinnedIds.size),
                        expanded = uiState.pinnedExpanded,
                        onToggleExpand = { uiState.pinnedExpanded = !uiState.pinnedExpanded }
                    )
                }
                if (uiState.pinnedExpanded) {
                    items(pinnedIds, key = { "pinned_$it" }, contentType = { "book_card" }) { id ->
                        val info = bookInfoMap[id] ?: BookInformation.empty(id)
                        BookshelfBookCard(
                            id = id,
                            info = info,
                            selected = selectedBookIdSet.contains(id),
                            selectMode = uiState.selectMode,
                            latestChapterTitle = null,
                            titleHeight = titleHeight,
                            shimmer = shimmerInstance,
                            onBookClick = uiState.onBookClick,
                            onBookSelect = uiState.changeBookSelectState,
                            onLongPress = onLongPress
                        )
                    }
                }
            }

            if (visibleAllIds.isNotEmpty()) {
                stickyHeader {
                    CollapseHeader(
                        icon = painterResource(R.drawable.outline_bookmark_24px),
                        title = stringResource(R.string.bookshelf_group_title_all, visibleAllIds.size),
                        expanded = uiState.allExpanded,
                        onToggleExpand = { uiState.allExpanded = !uiState.allExpanded }
                    )
                }
                if (uiState.allExpanded) {
                    items(visibleAllIds, key = { "book_$it" }, contentType = { "book_card" }) { id ->
                        val info = bookInfoMap[id] ?: BookInformation.empty(id)
                        BookshelfBookCard(
                            id = id,
                            info = info,
                            selected = selectedBookIdSet.contains(id),
                            selectMode = uiState.selectMode,
                            latestChapterTitle = null,
                            titleHeight = titleHeight,
                            shimmer = shimmerInstance,
                            onBookClick = uiState.onBookClick,
                            onBookSelect = uiState.changeBookSelectState,
                            onLongPress = onLongPress
                        )
                    }
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.padding(vertical = 18.dp),
                                text = stringResource(R.string.n_books, allBookIds.size),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.W600,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }

            navigationBarSpacer()
            bottomBarSpacer()
        }
    }
}

@Composable
private fun BookshelfBookCard(
    id: String,
    info: BookInformation,
    selected: Boolean,
    selectMode: Boolean,
    latestChapterTitle: String?,
    titleHeight: androidx.compose.ui.unit.Dp,
    shimmer: com.valentinilk.shimmer.Shimmer,
    onBookClick: (String) -> Unit,
    onBookSelect: (String) -> Unit,
    onLongPress: (String) -> Unit,
) {
    BookCardItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 6.dp),
        bookInformation = info,
        selected = selected,
        collected = false,
        onClick = {
            if (!selectMode) onBookClick(id)
            else onBookSelect(id)
        },
        onLongPress = { onLongPress(id) },
        latestChapterTitle = latestChapterTitle,
        shimmer = shimmer,
        titleHeight = titleHeight
    )
}

@Composable
private fun CollapseHeader(
    icon: androidx.compose.ui.graphics.painter.Painter,
    title: String,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable(onClick = onToggleExpand)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.W600,
                    modifier = Modifier.weight(1f)
                )
                val rotation by animateFloatAsState(if (expanded) 0f else 180f)
                Icon(
                    modifier = Modifier
                        .rotate(rotation)
                        .padding(8.dp),
                    painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                    contentDescription = "expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
    }
}

private fun sortBookIds(
    sourceIds: List<String>,
    allBookIds: List<String>,
    sortType: BookshelfSortType,
    sortReversed: Boolean,
    bookInfoMap: Map<String, BookInformation>,
    bookMetadataMap: Map<String, BookshelfBookMetadata?>
): List<String> {
    val stableIndexMap = allBookIds.withIndex().associate { it.value to it.index }
    val sourceIdSet = sourceIds.toHashSet()
    val locale = Locale.getDefault()
    val collator = Collator.getInstance(locale)
    val sortedIds = when (sortType) {
        BookshelfSortType.Default -> allBookIds.filter(sourceIdSet::contains)
        BookshelfSortType.Latest -> sourceIds.sortedWith(
            compareByDescending<String> {
                bookMetadataMap[it]?.lastUpdate ?: LocalDateTime.MIN
            }.thenBy { stableIndexMap[it] ?: Int.MAX_VALUE }
        )
        BookshelfSortType.Name -> sourceIds.sortedWith(
            Comparator { left, right ->
                val leftTitle = bookInfoMap[left]?.title.orEmpty()
                val rightTitle = bookInfoMap[right]?.title.orEmpty()
                val nameCompare = collator.compare(
                    titleSortKey(leftTitle, locale),
                    titleSortKey(rightTitle, locale)
                )
                if (nameCompare != 0) {
                    nameCompare
                } else {
                    (stableIndexMap[left] ?: Int.MAX_VALUE).compareTo(stableIndexMap[right] ?: Int.MAX_VALUE)
                }
            }
        )
        BookshelfSortType.WordCount -> sourceIds.sortedWith(
            compareByDescending<String> {
                bookInfoMap[it]?.wordCount?.count ?: Int.MIN_VALUE
            }.thenBy { stableIndexMap[it] ?: Int.MAX_VALUE }
        )
    }
    return if (sortType != BookshelfSortType.Default && sortReversed) {
        sortedIds.reversed()
    } else {
        sortedIds
    }
}

private fun titleSortKey(
    title: String,
    locale: Locale
): String {
    if (title.any { Pinyin.isChinese(it) }) {
        return Pinyin.toPinyin(title, "").lowercase(locale)
    }
    return title.lowercase(locale)
}
