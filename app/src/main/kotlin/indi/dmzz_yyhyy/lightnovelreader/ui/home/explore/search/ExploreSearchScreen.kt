package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.BookCardItem
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreUiState
import indi.dmzz_yyhyy.lightnovelreader.utils.LocalSnackbarHost
import indi.dmzz_yyhyy.lightnovelreader.utils.addToBookshelfAction
import indi.dmzz_yyhyy.lightnovelreader.utils.withHaptic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreSearchScreen(
    exploreUiState: ExploreUiState,
    exploreSearchUiState: ExploreSearchUiState,
    refresh: () -> Unit,
    requestAddBookToBookshelf: (String) -> Unit,
    onClickBack: () -> Unit,
    init: () -> Unit,
    onChangeSearchType: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClickDeleteHistory: (String) -> Unit,
    onClickClearAllHistory: () -> Unit,
    onClickBook: (String) -> Unit,
    updateSuggestions: (keyword: String) -> Unit
) {
    var searchKeyword by rememberSaveable { mutableStateOf("") }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        init.invoke()
    }
    Scaffold(
        topBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .semantics { isTraversalGroup = true }) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .height(56.dp)) {
                    DropdownMenu(
                        offset = DpOffset((-12).dp, 0.dp),
                        expanded = exploreSearchUiState.dropdownMenuExpanded,
                        onDismissRequest = { exploreSearchUiState.setDropdownMenuExpandedState(false) }) {
                        exploreSearchUiState.searchTypeIdList.forEach {
                            DropdownMenuItem(
                                text = {
                                    exploreSearchUiState.searchTypeNameMap[it]?.let { it1 ->
                                        Text(
                                            text = it1.resolve(),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                },
                                onClick = {
                                    exploreSearchUiState.setDropdownMenuExpandedState(false)
                                    onChangeSearchType(it)
                                }
                            )
                        }
                    }
                }
                SearchBar(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(horizontal = if (!exploreSearchUiState.searchBarExpanded) 12.dp else 0.dp)
                        .semantics { traversalIndex = 0f },
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchKeyword,
                            onQueryChange = {
                                searchKeyword = it
                                updateSuggestions(it)
                            },
                            onSearch = {
                                exploreSearchUiState.setSearchBarExpandedState(false)
                                onSearch(it)
                            },
                            expanded = exploreSearchUiState.searchBarExpanded,
                            onExpandedChange = exploreSearchUiState::setSearchBarExpandedState,
                            placeholder = { AnimatedText(
                                text = exploreSearchUiState.searchTip.resolve(),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ) },
                            leadingIcon = {
                                IconButton(onClick = onClickBack) {
                                    Icon(painter = painterResource(R.drawable.arrow_back_24px), contentDescription = "back")
                                }
                            },
                            trailingIcon = {
                                Row {
                                    if (searchKeyword.isNotBlank())
                                        IconButton(onClick = {
                                            exploreSearchUiState.setSearchBarExpandedState(true)
                                            searchKeyword = ""
                                        }) {
                                            Icon(painter = painterResource(R.drawable.close_24px), contentDescription = "clear")
                                        }
                                    if (exploreSearchUiState.searchBarExpanded)
                                        IconButton(onClick = { exploreSearchUiState.setDropdownMenuExpandedState(true) }) {
                                            Icon(painter = painterResource(R.drawable.filter_alt_24px), contentDescription = "filter")
                                        }
                                }
                            },
                        )
                    },
                    expanded = exploreSearchUiState.searchBarExpanded,
                    onExpandedChange = { if (!it) onClickBack.invoke() }
                ) {
                    val hasHistory = exploreSearchUiState.historyList.isNotEmpty()
                    val showHistory = exploreSearchUiState.suggestions.isEmpty() || searchKeyword.isEmpty()
                    AnimatedVisibility(
                        visible = !hasHistory && showHistory,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        EmptyPage(
                            icon = painterResource(R.drawable.schedule_90dp),
                            title = stringResource(R.string.nothing_here),
                            description = stringResource(R.string.nothing_here_desc_search)
                        )
                    }
                    AnimatedVisibility(
                        visible = hasHistory && showHistory,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            Modifier
                                .padding(vertical = 8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = stringResource(id = R.string.search_history),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.W600
                                )

                                Box(Modifier.weight(2f))

                                TextButton (
                                    onClick = onClickClearAllHistory,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.clear_all),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.W600,
                                    )
                                }
                            }

                            Box(Modifier.height(8.dp))

                            exploreSearchUiState.historyList.forEach { history ->
                                if (history.isEmpty()) return@forEach
                                AnimatedContent(
                                    targetState = history,
                                    label = "HistoryItemAnimation"
                                ) {
                                    Row (
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                            .padding(horizontal = 16.dp)
                                            .clickable {
                                                searchKeyword = it
                                                exploreSearchUiState.setSearchBarExpandedState(false)
                                                onSearch.invoke(history)
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(start = 8.dp),
                                            text = it,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Box(Modifier.weight(2f))
                                        IconButton(onClick = { onClickDeleteHistory(history) }) {
                                            Icon(
                                                painter = painterResource(R.drawable.close_24px),
                                                contentDescription = "delete",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = !showHistory,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            Modifier
                                .padding(vertical = 8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            exploreSearchUiState.suggestions.forEach { history ->
                                if (history.isEmpty()) return@forEach
                                AnimatedContent(
                                    targetState = history,
                                    label = "SuggestionsItemAnimation"
                                ) {
                                    Row (
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                            .padding(horizontal = 16.dp)
                                            .clickable {
                                                searchKeyword = it
                                                exploreSearchUiState.setSearchBarExpandedState(false)
                                                onSearch.invoke(history)
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(start = 8.dp),
                                            text = it,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
            AnimatedVisibility(
                visible = exploreSearchUiState.errorMessage.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyPage(
                    icon = painterResource(R.drawable.error_24px),
                    title = "搜索出现了错误",
                    description = exploreSearchUiState.errorMessage
                )
            }
            AnimatedVisibility(
                visible = exploreSearchUiState.isLoadingComplete && exploreSearchUiState.searchResult.isEmpty() && exploreSearchUiState.errorMessage.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyPage(
                    icon = painterResource(R.drawable.not_found_90dp),
                    title = stringResource(R.string.search_no_results),
                    description = stringResource(R.string.search_no_results_desc)
                )
            }
            AnimatedVisibility(
                visible = !exploreSearchUiState.isLoading && exploreSearchUiState.errorMessage.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val density = LocalDensity.current
                val lineHeight = MaterialTheme.typography.titleMedium.lineHeight
                val titleHeight = with(density) {
                    (lineHeight * 2.2f).toDp()
                }
                LazyColumn {
                    stickyHeader {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(top = 8.dp)
                        ) {
                            AnimatedText(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp),
                                text = stringResource(
                                    R.string.search_results_title,
                                    searchKeyword,
                                    exploreSearchUiState.searchResult.size,
                                    if (exploreSearchUiState.isLoadingComplete) "" else "..."
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.W600,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    items(exploreSearchUiState.searchResult) {
                        val addToBookshelf = addToBookshelfAction.toSwipeAction {
                            requestAddBookToBookshelf(it.first)
                        }
                        BookCardItem(
                            modifier = Modifier.padding(horizontal = 16.dp).padding(vertical = 3.dp),
                            bookInformationFlow = it.second,
                            onClick = { onClickBook(it.first) },
                            onLongPress = withHaptic {},
                            collected = exploreSearchUiState.allBookshelfBookIds.contains(it.first),
                            swipeToRightActions = listOf(addToBookshelf),
                            titleHeight = titleHeight
                        )
                    }
                    item {
                        AnimatedVisibility(
                            visible = !exploreSearchUiState.isLoadingComplete,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}