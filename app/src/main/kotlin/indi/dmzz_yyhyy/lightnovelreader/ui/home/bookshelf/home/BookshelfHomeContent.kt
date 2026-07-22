package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import com.github.promeg.pinyinhelper.Pinyin
import com.valentinilk.shimmer.Shimmer
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer
import com.valentinilk.shimmer.unclippedBoundsInWindow
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.BookshelfBookItem
import indi.dmzz_yyhyy.lightnovelreader.utils.bottomBarPadding
import indi.dmzz_yyhyy.lightnovelreader.utils.bottomBarSpacer
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfSortType
import io.nightfish.lightnovelreader.api.error.WebRequestError
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.text.Collator
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BookshelfHomeContent(
    uiState: BookshelfHomeUiState,
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

        val selectedBookshelfUiState = uiState.selectedBookshelf
        if (selectedBookshelfUiState != null) {
//            val updatedIds = sortBooks(
//                sourceIds = uiState.selectedBookshelf.updatedBookIds,
//                allBookIds = allBookIds,
//                sortType = uiState.selectedBookshelf.sortType,
//                sortReversed = uiState.selectedBookshelf.sortReversed,
//            )
//            val pinnedIds = sortBooks(
//                sourceIds = uiState.selectedBookshelf.pinnedBookIds,
//                allBookIds = allBookIds,
//                sortType = uiState.selectedBookshelf.sortType,
//                sortReversed = uiState.selectedBookshelf.sortReversed,
//            )
//            val visibleAllIds = sortBooks(
//                sourceIds = allBookIds,
//                allBookIds = allBookIds,
//                sortType = uiState.selectedBookshelf.sortType,
//                sortReversed = uiState.selectedBookshelf.sortReversed,
//            )
            val allBookIds = remember(selectedBookshelfUiState.allBookFlows) {
                selectedBookshelfUiState.allBookFlows.map { it.first }
            }
            val allBooksFlow = remember(selectedBookshelfUiState.allBookFlows) {
                selectedBookshelfUiState.allBookFlows
                    .map { pair ->
                        pair.second.map {
                            pair.first to it
                        }
                    }
                    .let { flows ->
                        combine(flows) {
                            it.toList()
                        }
                    }
            }
            val allBooks by allBooksFlow.collectAsStateWithLifecycle(emptyList())
            val sortedAllBooks = remember(allBooks, selectedBookshelfUiState.sortType, selectedBookshelfUiState.sortReversed) {
                sortBooks(
                    source = allBooks,
                    allBookIds = allBookIds,
                    sortType = selectedBookshelfUiState.sortType,
                    sortReversed = selectedBookshelfUiState.sortReversed
                )
            }

            val updatedBooksFlow = remember(selectedBookshelfUiState.updatedBookFlows) {
                selectedBookshelfUiState.updatedBookFlows
                    .map { pair ->
                        pair.second.map {
                            pair.first to it
                        }
                    }
                    .let { flows ->
                        combine(flows) {
                            it.toList()
                        }
                    }
            }
            val updatedBooks by updatedBooksFlow.collectAsStateWithLifecycle(emptyList())
            val sortedUpdatedBooks = remember(updatedBooks, selectedBookshelfUiState.sortType, selectedBookshelfUiState.sortReversed) {
                sortBooks(
                    source = updatedBooks,
                    allBookIds = allBookIds,
                    sortType = selectedBookshelfUiState.sortType,
                    sortReversed = selectedBookshelfUiState.sortReversed
                )
            }

            val pinnedBooksFlow = remember(selectedBookshelfUiState.pinnedBookFlows) {
                selectedBookshelfUiState.pinnedBookFlows
                    .map { pair ->
                        pair.second.map {
                            pair.first to it
                        }
                    }
                    .let { flows ->
                        combine(flows) {
                            it.toList()
                        }
                    }
            }
            val pinnedBooks by pinnedBooksFlow.collectAsStateWithLifecycle(emptyList())
            val sortedPinnedBooks = remember(pinnedBooks, selectedBookshelfUiState.sortType, selectedBookshelfUiState.sortReversed) {
                sortBooks(
                    source = pinnedBooks,
                    allBookIds = allBookIds,
                    sortType = selectedBookshelfUiState.sortType,
                    sortReversed = selectedBookshelfUiState.sortReversed
                )
            }

            val selectedBookIdSet = uiState.selectedBookIds.toHashSet()
            val onLongPress: (String) -> Unit = { bookId ->
                if (!uiState.selectMode) {
                    uiState.onEnableSelectMode()
                }
                uiState.changeBookSelectState(bookId)
            }
            var initialScrollApplied by remember(uiState.selectedBookshelfId) { mutableStateOf(false) }

            LaunchedEffect(uiState.selectedBookshelfId, selectedBookshelfUiState.allBookFlows) {
                if (initialScrollApplied || selectedBookshelfUiState.allBookFlows.isEmpty()) return@LaunchedEffect
                listState.scrollToItem(0)
                initialScrollApplied = true
            }

            val shimmerInstance = rememberShimmer(ShimmerBounds.Custom)
            val density = LocalDensity.current
            val lineHeight = MaterialTheme.typography.titleMedium.lineHeight
            val titleHeight = with(density) { (lineHeight * 2.2f).toDp() }

            AnimatedVisibility(
                visible = uiState.selectedBookshelf?.allBookFlows?.isEmpty() == true,
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
                bookshelfContent(
                    selectedBookIdSet = selectedBookIdSet,
                    titleHeight = titleHeight,
                    shimmer = shimmerInstance,
                    isSelectMode = uiState.selectMode,
                    onClickBook = uiState.onBookClick,
                    onBookSelect = uiState.changeBookSelectState,
                    onLongPress = onLongPress,
                    updatedBooks = sortedUpdatedBooks,
                    updatedExpanded = uiState.updatedExpanded,
                    onToggleUpdateExpand = { uiState.updatedExpanded = !uiState.updatedExpanded },
                    pinnedBooks = sortedPinnedBooks,
                    pinnedExpanded = uiState.pinnedExpanded,
                    onTogglePinnedExpand = { uiState.pinnedExpanded = !uiState.pinnedExpanded },
                    allBooks = sortedAllBooks,
                    allExpanded = uiState.allExpanded,
                    onToggleAllExpand = { uiState.allExpanded = !uiState.allExpanded }
                )
                navigationBarSpacer()
                bottomBarSpacer()
            }
        }
    }
}

private fun LazyListScope.bookshelfContent(
    selectedBookIdSet: Set<String>,
    titleHeight: Dp,
    shimmer: Shimmer,
    isSelectMode: Boolean,
    onClickBook: (String) -> Unit,
    onBookSelect: (String) -> Unit,
    onLongPress: (String) -> Unit,
    updatedBooks: List<Pair<String, Result<BookshelfBookItem, WebRequestError>?>>,
    updatedExpanded: Boolean,
    onToggleUpdateExpand: () -> Unit,
    pinnedBooks: List<Pair<String, Result<BookshelfBookItem, WebRequestError>?>>,
    pinnedExpanded: Boolean,
    onTogglePinnedExpand: () -> Unit,
    allBooks: List<Pair<String, Result<BookshelfBookItem, WebRequestError>?>>,
    allExpanded: Boolean,
    onToggleAllExpand: () -> Unit
) {
    if (updatedBooks.isNotEmpty()) {
        stickyHeader {
            CollapseHeader(
                icon = painterResource(R.drawable.autorenew_24px),
                title = stringResource(R.string.bookshelf_group_title_updated, updatedBooks.size),
                expanded = updatedExpanded,
                onToggleExpand = onToggleUpdateExpand
            )
        }
        if (updatedExpanded) {
            items(
                updatedBooks,
                key = { "updated_${it.first}" },
                contentType = { "book_card" }
            ) { pair ->
                BookshelfBookCard(
                    id = pair.first,
                    bookshelfBookItem = pair.second,
                    selected = selectedBookIdSet.contains(pair.first),
                    selectMode = isSelectMode,
                    titleHeight = titleHeight,
                    shimmer = shimmer,
                    onBookClick = onClickBook,
                    onBookSelect = onBookSelect,
                    onLongPress = onLongPress
                )
            }
        }
    }

    if (pinnedBooks.isNotEmpty()) {
        stickyHeader {
            CollapseHeader(
                icon = painterResource(R.drawable.keep_24px),
                title = stringResource(R.string.bookshelf_group_title_pinned, pinnedBooks.size),
                expanded = pinnedExpanded,
                onToggleExpand = onTogglePinnedExpand
            )
        }
        if (pinnedExpanded) {
            items(
                pinnedBooks,
                key = { "pinned_${it.first}" },
                contentType = { "book_card" }
            ) { pair ->
                BookshelfBookCard(
                    id = pair.first,
                    bookshelfBookItem = pair.second,
                    selected = selectedBookIdSet.contains(pair.first),
                    selectMode = isSelectMode,
                    titleHeight = titleHeight,
                    shimmer = shimmer,
                    onBookClick = onClickBook,
                    onBookSelect = onBookSelect,
                    onLongPress = onLongPress
                )
            }
        }
    }

    if (allBooks.isNotEmpty()) {
        stickyHeader {
            CollapseHeader(
                icon = painterResource(R.drawable.outline_bookmark_24px),
                title = stringResource(R.string.bookshelf_group_title_all, allBooks.size),
                expanded = allExpanded,
                onToggleExpand = onToggleAllExpand
            )
        }
        if (allExpanded) {
            items(
                allBooks,
                key = { "book_${it.first}" },
                contentType = { "book_card" }
            ) { pair ->
                BookshelfBookCard(
                    id = pair.first,
                    bookshelfBookItem = pair.second,
                    selected = selectedBookIdSet.contains(pair.first),
                    selectMode = isSelectMode,
                    titleHeight = titleHeight,
                    shimmer = shimmer,
                    onBookClick = onClickBook,
                    onBookSelect = onBookSelect,
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
                        text = stringResource(R.string.n_books, allBooks.size),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.W600,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun BookshelfBookCard(
    id: String,
    bookshelfBookItem: Result<BookshelfBookItem, WebRequestError>?,
    selected: Boolean,
    selectMode: Boolean,
    titleHeight: Dp,
    shimmer: Shimmer,
    onBookClick: (String) -> Unit,
    onBookSelect: (String) -> Unit,
    onLongPress: (String) -> Unit,
) {
    Crossfade(
        targetState = bookshelfBookItem,
        label = "BookCardCrossfade"
    ) { result ->
        result?.onOk {
            BookCardContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 6.dp),
                bookInformation = it.bookInformation,
                selected = selected,
                collected = false,
                onClick = {
                    if (!selectMode) onBookClick(id)
                    else onBookSelect(id)
                },
                onLongPress = { onLongPress(id) },
                latestChapterTitle = it.lastUpdatedChapterTitle,
                titleHeight = titleHeight
            )
        }?.onErr {
            //TODO 错误显示
        } ?: BookCardContentSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(vertical = 6.dp)
                .shimmer(shimmer)
        )
    }
}

@Composable
private fun CollapseHeader(
    icon: Painter,
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

private fun sortBooks(
    source: List<Pair<String, Result<BookshelfBookItem, WebRequestError>>>,
    allBookIds: List<String>,
    sortType: BookshelfSortType,
    sortReversed: Boolean,
): List<Pair<String, Result<BookshelfBookItem, WebRequestError>>> {
    val stableIndexMap = allBookIds.withIndex().associate { it.value to it.index }
    val locale = Locale.getDefault()
    val collator = Collator.getInstance(locale)
    val sorted = when (sortType) {
        BookshelfSortType.Default -> source.sortedBy {
            stableIndexMap[it.first] ?: Int.MAX_VALUE
        }
        BookshelfSortType.Latest -> source.sortedWith(
            compareByDescending<Pair<String, Result<BookshelfBookItem, WebRequestError>>> { pair ->
                pair.second.map { it.bookInformation.lastUpdated }.get()
            }.thenBy { stableIndexMap[it.first] ?: Int.MAX_VALUE }
        )
        BookshelfSortType.Name -> source.sortedWith(
            Comparator { left, right ->
                val leftTitle = left.second.map { it.bookInformation.title }.getOrElse { "" }
                val rightTitle = right.second.map { it.bookInformation.title }.getOrElse { "" }
                val nameCompare = collator.compare(
                    titleSortKey(leftTitle, locale),
                    titleSortKey(rightTitle, locale)
                )
                if (nameCompare != 0) {
                    nameCompare
                } else {
                    (stableIndexMap[left.first] ?: Int.MAX_VALUE).compareTo(stableIndexMap[right.first] ?: Int.MAX_VALUE)
                }
            }
        )
        BookshelfSortType.WordCount -> source.sortedWith(
            compareByDescending<Pair<String, Result<BookshelfBookItem, WebRequestError>>> { pair ->
                pair.second.map { it.bookInformation.wordCount.count }.getOrElse { 0 }
            }.thenBy { stableIndexMap[it.first] ?: Int.MAX_VALUE }
        )
    }
    return if (sortType != BookshelfSortType.Default && sortReversed) {
        sorted.reversed()
    } else {
        sorted
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
