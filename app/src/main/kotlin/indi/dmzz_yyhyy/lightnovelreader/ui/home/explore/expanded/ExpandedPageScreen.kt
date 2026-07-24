package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.expanded

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.BookCardItem
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Component
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreUiState
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.addToBookshelfAction
import indi.dmzz_yyhyy.lightnovelreader.utils.fadingEdge
import indi.dmzz_yyhyy.lightnovelreader.utils.withHaptic
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedPageScreen(
    exploreUiState: ExploreUiState,
    expandedPageUiState: ExpandedPageUiState,
    dialog: (@Composable () -> Unit) -> Unit,
    expandedPageDataSourceId: String,
    init: (String) -> Unit,
    refreshResult: () -> Unit,
    loadMore: () -> Unit,
    requestAddBookToBookshelf: (String) -> Unit,
    onClickBack: () -> Unit,
    onClickBook: (String) -> Unit,
    refresh: () -> Unit,
) {
    val rememberPullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()
    val enterAlwaysScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var isRefreshing by remember{ mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        init.invoke(expandedPageDataSourceId)
    }

    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val lineHeight = MaterialTheme.typography.titleMedium.lineHeight
    val titleHeight = with(density) {
        (lineHeight * 2.2f).toDp()
    }

    Scaffold(
        topBar = {
            TopBar(
                scrollBehavior = enterAlwaysScrollBehavior,
                title = expandedPageUiState.pageTitle,
                onClickBack = onClickBack
            )
        },
        snackbarHost = {
            SnackbarHost(LocalSnackbarHost.current)
        }
    ) { paddingValues ->
        ExploreScreen(
            modifier = Modifier.padding(paddingValues),
            uiState = exploreUiState,
            refresh = refresh
        ) {
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize(),
                isRefreshing = isRefreshing,
                state = rememberPullToRefreshState,
                onRefresh = {
                    isRefreshing = true
                    refresh()
                    isRefreshing = false
                    scope.launch {
                        rememberPullToRefreshState.animateToHidden()
                    }
                }
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(enterAlwaysScrollBehavior.nestedScrollConnection)
                        .background(MaterialTheme.colorScheme.surface),
                    contentPadding = PaddingValues(vertical = 3.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fadingEdge(
                                    Brush.horizontalGradient(
                                        0.02f to Color.Transparent,
                                        0.05f to Color.White,
                                        0.95f to Color.White,
                                        0.98f to Color.Transparent
                                    )
                                ),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item {
                                Spacer(Modifier.width(8.dp))
                            }
                            items(expandedPageUiState.filters) {
                                it.Component(dialog, refreshResult)
                            }
                            item {
                                Spacer(Modifier.width(8.dp))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    item {
                        AnimatedVisibility(
                            visible = expandedPageUiState.bookList.isEmpty(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    items(
                        items = expandedPageUiState.bookList,
                        key = { it.first }
                    ) { pair ->
                        val addToBookshelf = addToBookshelfAction.toSwipeAction {
                            requestAddBookToBookshelf(pair.first)
                        }
                        BookCardItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            bookInformationFlow = pair.second,
                            onClick = { onClickBook(pair.first) },
                            onLongPress = withHaptic {},
                            collected = expandedPageUiState.allBookshelfBookIds.contains(
                                pair.first
                            ),
                            swipeToRightActions = listOf(addToBookshelf),
                            titleHeight = titleHeight
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        }.collect { lastVisibleIndex ->
            val size = expandedPageUiState.bookList.size
            if (size > 0 && lastVisibleIndex >= size - 3) {
                loadMore()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    title: String,
    onClickBack: () -> Unit
) {
    MediumTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.nav_explore_child, title),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.W600,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
        /*actions = {
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(id = R.drawable.more_vert_24px),
                    contentDescription = "more"
                )
            }
        },*/
        scrollBehavior = scrollBehavior
    )
}