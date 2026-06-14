package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.reorder

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home.BookshelfHomeUiState
import indi.dmzz_yyhyy.lightnovelreader.utils.bottomBarSpacer
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.bookshelf.Bookshelf
import kotlinx.coroutines.flow.StateFlow
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfReorderBooksScreen(
    bookshelfId: Int,
    uiState: BookshelfHomeUiState,
    prepare: (Int) -> Unit,
    onExit: () -> Unit,
    getBookInfoFlow: (String) -> StateFlow<BookInformation>,
    moveBook: (Int, Int) -> Unit,
    onClickBack: () -> Unit,
) {
    BackHandler {
        onExit()
        onClickBack()
    }
    LaunchedEffect(bookshelfId) {
        prepare(bookshelfId)
    }
    Column {
        ReorderTopBar(
            title = stringResource(R.string.bookshelf_adjust_order),
            onBack = {
                onExit()
                onClickBack()
            }
        )
        BookshelfReorderContent(
            reorderBookIds = uiState.reorderBookIds,
            nestedScrollConnection = TopAppBarDefaults.pinnedScrollBehavior().nestedScrollConnection,
            getBookInfoFlow = getBookInfoFlow,
            moveBook = moveBook,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookshelfReorderBookshelvesScreen(
    uiState: BookshelfHomeUiState,
    prepare: () -> Unit,
    onExit: (List<Int>) -> Unit,
    onClickBack: () -> Unit,
    onClickEditBookshelf: (Int) -> Unit
) {
    var reorderedBookshelfIds by rememberSaveable { mutableStateOf(emptyList<Int>()) }

    BackHandler {
        onExit(reorderedBookshelfIds)
        onClickBack()
    }
    LaunchedEffect(uiState.bookshelfList) {
        prepare()
        val currentBookshelfIds = uiState.bookshelfList.map { it.id }
        if (reorderedBookshelfIds.isEmpty()) {
            reorderedBookshelfIds = currentBookshelfIds
            return@LaunchedEffect
        }
        val currentBookshelfIdSet = currentBookshelfIds.toHashSet()
        val preservedBookshelfIds = reorderedBookshelfIds.filter(currentBookshelfIdSet::contains)
        val newBookshelfIds = currentBookshelfIds.filterNot(preservedBookshelfIds::contains)
        reorderedBookshelfIds = preservedBookshelfIds + newBookshelfIds
    }
    Column {
        ReorderTopBar(
            title = stringResource(R.string.bookshelf_adjust_bookshelf_order),
            onBack = {
                onExit(reorderedBookshelfIds)
                onClickBack()
            }
        )
        BookshelfListReorderContent(
            reorderBookshelfIds = reorderedBookshelfIds,
            bookshelfList = uiState.bookshelfList,
            nestedScrollConnection = TopAppBarDefaults.pinnedScrollBehavior().nestedScrollConnection,
            moveBookshelf = { fromIndex, toIndex ->
                reorderedBookshelfIds = reorderedBookshelfIds.toMutableList().apply {
                    add(toIndex, removeAt(fromIndex))
                }
            },
            onClickEditBookshelf = onClickEditBookshelf
        )
    }
}


@Composable
fun BookshelfReorderContent(
    reorderBookIds: List<String>,
    nestedScrollConnection: NestedScrollConnection,
    getBookInfoFlow: (String) -> StateFlow<BookInformation>,
    moveBook: (Int, Int) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        scrollThresholdPadding = WindowInsets.systemBars.asPaddingValues()
    ) { from, to ->
        moveBook(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection),
        state = lazyListState,
        contentPadding = PaddingValues(top = 6.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(
            items = reorderBookIds,
            key = { it }
        ) { id ->
            ReorderableItem(reorderableLazyListState, key = id) { isDragging ->
                val infoFlow = remember(id) { getBookInfoFlow(id) }
                val info by infoFlow.collectAsStateWithLifecycle()
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .zIndex(if (isDragging) 1f else 0f),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = elevation
                ) {
                    Column {
                        Row(
                            modifier = Modifier.padding(all = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 60.dp, height = 88.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                Cover(
                                    width = 60.dp,
                                    height = 88.dp,
                                    uri = info.coverUri
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = info.title,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.W600
                                )
                                Text(
                                    text = info.author,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.W500,
                                    letterSpacing = 0.15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                modifier = Modifier.draggableHandle(
                                    onDragStarted = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                    },
                                    onDragStopped = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                    }
                                ),
                                onClick = {}
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.drag_indicator_24px),
                                    contentDescription = stringResource(R.string.bookshelf_adjust_order),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 18.dp),
                    text = stringResource(R.string.n_books, reorderBookIds.size),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.W600,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        navigationBarSpacer()
        bottomBarSpacer()
    }
}

@Composable
fun BookshelfListReorderContent(
    reorderBookshelfIds: List<Int>,
    bookshelfList: List<Bookshelf>,
    nestedScrollConnection: NestedScrollConnection,
    moveBookshelf: (Int, Int) -> Unit,
    onClickEditBookshelf: (Int) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val bookshelfMap = bookshelfList.associateBy { it.id }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        scrollThresholdPadding = WindowInsets.systemBars.asPaddingValues()
    ) { from, to ->
        moveBookshelf(from.index, to.index)
        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp)
            .nestedScroll(nestedScrollConnection),
        state = lazyListState,
        contentPadding = PaddingValues(top = 6.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(
            items = reorderBookshelfIds,
            key = { it }
        ) { id ->
            val bookshelf = bookshelfMap[id] ?: return@items
            ReorderableItem(reorderableLazyListState, key = id) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp, label = "bookshelfElevation")

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .zIndex(if (isDragging) 1f else 0f),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = elevation
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = bookshelf.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.W600
                            )
                            Text(
                                text = stringResource(R.string.n_books, bookshelf.allBookIds.size),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onClickEditBookshelf(bookshelf.id) }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.edit_square_24px),
                                contentDescription = stringResource(R.string.bookshelf_adjust_bookshelf_order),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            modifier = Modifier.draggableHandle(
                                onDragStarted = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                                },
                                onDragStopped = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                }
                            ),
                            onClick = {}
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.drag_indicator_24px),
                                contentDescription = stringResource(R.string.bookshelf_adjust_bookshelf_order),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        navigationBarSpacer()
        bottomBarSpacer()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReorderTopBar(
    title: String,
    onBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        }
    )
}
