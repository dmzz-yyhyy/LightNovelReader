package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.expanded

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Component
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.ExplorationBookCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandedPageScreen(
    topBar: (@Composable (TopAppBarScrollBehavior, TopAppBarScrollBehavior) -> Unit) -> Unit,
    expandedPageDataSourceId: String,
    uiState: ExpandedPageUiState,
    init: (String) -> Unit,
    loadMore: () -> Unit,
    onClickBack: () -> Unit,
    onClickBook: (Int) -> Unit,
) {
    LaunchedEffect(expandedPageDataSourceId) { init(expandedPageDataSourceId) }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        topBar { enterAlwaysScrollBehavior, _ ->
            TopBar(
                scrollBehavior =  enterAlwaysScrollBehavior,
                title = uiState.pageTitle,
                onClickBack = onClickBack
            )
        }
    }
    AnimatedVisibility(
        visible = uiState.bookList.isEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Loading()
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            LazyRow(Modifier.padding(start = 12.dp)) {
                items(uiState.filters) {
                    it.Component()
                }
            }
            Box(Modifier.height(3.dp))
        }
        itemsIndexed(uiState.bookList) { index, bookInformation ->
            ExplorationBookCard(
                modifier = Modifier
                    .padding(start = 19.dp, end = 10.dp)
                    .animateItem(),
                bookInformation = bookInformation,
                onClickBook = onClickBook
            )
            LaunchedEffect(uiState.bookList.size) {
                if (uiState.bookList.size - index == 3) { loadMore.invoke() }
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
                text = "探索 · $title",
                style = MaterialTheme.typography.titleLarge,
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
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(id = R.drawable.more_vert_24px),
                    contentDescription = "more"
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
}