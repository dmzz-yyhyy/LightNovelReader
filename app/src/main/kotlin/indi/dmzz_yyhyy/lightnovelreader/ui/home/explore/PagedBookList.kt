package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.michaelbull.result.map
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.components.BookCardItem
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.utils.addToBookshelfAction
import indi.dmzz_yyhyy.lightnovelreader.utils.withHaptic
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.PageBook
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter
import io.nightfish.lightnovelreader.api.web.explore.filter.FilterValues
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun PagedBookList(
    state: BookPageState,
    filters: List<Filter<*>>,
    filterValues: () -> FilterValues,
    updateFilters: (FilterValues) -> Unit,
    append: () -> Unit,
    retry: () -> Unit,
    bookshelfIds: Set<Identifier.Book>,
    processBook: (BookInformation) -> BookInformation,
    onOpenBook: (Identifier.Book) -> Unit,
    addToBookshelf: (Identifier.Book) -> Unit,
    modifier: Modifier = Modifier,
    heading: (@Composable () -> Unit)? = null
) {
    val listState = rememberLazyListState()
    val latestState by rememberUpdatedState(state)
    val latestAppend by rememberUpdatedState(append)
    var anchor by remember { mutableStateOf<BookAnchor?>(null) }
    var restoreAnchor by remember { mutableStateOf(false) }
    var generation by rememberSaveable { mutableLongStateOf(state.generation) }

    LaunchedEffect(state.generation) {
        if (generation != state.generation) {
            generation = state.generation
            anchor = null
            restoreAnchor = false
            listState.scrollToItem(0)
        }
    }
    LaunchedEffect(state.books) {
        val position = anchor
        if (restoreAnchor && position != null && state.books.isNotEmpty()) {
            val visibleIds = state.books.map(PageBook::id)
            val original = state.allBookIds.indexOf(position.id)
            val retained = visibleIds.toHashSet()
            val target = if (original >= 0) {
                state.allBookIds.drop(original).firstOrNull { it in retained }
                    ?: state.allBookIds.take(original).lastOrNull { it in retained }
            } else null
            val index = visibleIds.indexOf(target)
            if (index >= 0) listState.scrollToItem(index, position.offset)
        }
        restoreAnchor = false
    }

    val pagingScroll = remember(listState) {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = Offset.Zero.also {
                if (source != NestedScrollSource.UserInput) return@also
                if (consumed.y != 0f || available.y != 0f) {
                    anchor = null
                    restoreAnchor = false
                }
                if (consumed.y + available.y < 0f && latestState.canAppend) {
                    val size = latestState.books.size
                    val lastVisible =
                        listState.layoutInfo.visibleItemsInfo.lastOrNull { it.index < size }
                    if (size == 0 || (lastVisible != null && lastVisible.index >= size - 3)) latestAppend()
                }
                return Offset.Zero
            }
        }
    }

    Column(modifier.fillMaxSize()) {
        if (filters.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(filters, key = { it.key.id }) { filter ->
                    filter.Content(filterValues) { next ->
                        val previous = filterValues()
                        if (next.dataSource == previous.dataSource && next.local != previous.local) {
                            if (anchor == null) {
                                val layout = listState.layoutInfo
                                val first =
                                    layout.visibleItemsInfo.firstOrNull { it.key is Identifier.Book }
                                if (first != null) {
                                    anchor = BookAnchor(
                                        first.key as Identifier.Book,
                                        (layout.viewportStartOffset - first.offset).coerceAtLeast(0)
                                    )
                                }
                            }
                            restoreAnchor = true
                        }
                        updateFilters(next)
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp)
                    .padding(bottom = 2.dp)
            )
        }
        heading?.invoke() ?: Spacer(Modifier.height(8.dp))
        val lineHeight = MaterialTheme.typography.titleMedium.lineHeight
        val titleHeight = with(LocalDensity.current) { (lineHeight * 2.2f).toDp() }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .nestedScroll(pagingScroll),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.books, key = PageBook::id) { book ->
                val information = remember(book, processBook) {
                    flowOf(book.information.map(processBook))
                }
                BookCardItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    bookInformationFlow = information,
                    onClick = { onOpenBook(book.id) },
                    onLongPress = withHaptic {},
                    collected = book.id in bookshelfIds,
                    swipeToRightActions = listOf(addToBookshelfAction.toSwipeAction {
                        addToBookshelf(
                            book.id
                        )
                    }),
                    titleHeight = titleHeight
                )
            }
            if (state.loading) {
                item {
                    LinearProgressIndicator(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
            }
            state.error?.let { error ->
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            if (error.title.isNotBlank()) Text(
                                error.title,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(error.message, style = MaterialTheme.typography.bodyMedium)
                        }
                        TextButton(onClick = retry) { Text(stringResource(R.string.retry)) }
                    }
                }
            }
            if (state.canAppend && !state.loading && state.error == null && state.openBook == null) {
                item(key = "scroll-for-more") {
                    if (state.books.isEmpty()) {
                        ScrollForMoreHint(
                            onClick = latestAppend,
                            modifier = Modifier.fillParentMaxSize().navigationBarsPadding()
                        )
                    } else {
                        ScrollForMoreHint(
                            onClick = latestAppend,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        )
                    }
                }
            } else if (state.books.isEmpty() && state.error == null && !state.loading && state.openBook == null) {
                item {
                    EmptyPage(
                        modifier = Modifier.fillParentMaxSize().navigationBarsPadding(),
                        icon = painterResource(R.drawable.search_24px),
                        title = stringResource(R.string.search_no_results),
                        description = stringResource(R.string.search_no_results_desc)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrollForMoreHint(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEmpty: Boolean = false
) {
    val color = LocalAppTheme.current.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.keyboard_arrow_up_24px),
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(32.dp)
                .rotate(180f)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = if (isEmpty) stringResource(R.string.explore_scroll_for_more_empty)
            else stringResource(R.string.explore_scroll_for_more),
            style = MaterialTheme.typography.titleSmall,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

private data class BookAnchor(val id: Identifier.Book, val offset: Int)
