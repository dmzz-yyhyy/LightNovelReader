package indi.dmzz_yyhyy.lightnovelreader.ui.home.exploration.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorationSearchScreen(
    topBar: (@Composable (TopAppBarScrollBehavior, TopAppBarScrollBehavior) -> Unit) -> Unit,
    onCLickBack: () -> Unit,
    init: () -> Unit,
    onChangeSearchType: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClickDeleteHistory: (String) -> Unit,
    onClickClearAllHistory: () -> Unit,
    onClickBook: (Int) -> Unit,
    uiState: ExplorationSearchUiState
) {
    var searchKeyword by rememberSaveable { mutableStateOf("") }
    var searchBarExpanded by rememberSaveable { mutableStateOf(true) }
    var searchBarRect by remember { mutableStateOf(Rect.Zero) }
    var dropdownMenuExpanded by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        init.invoke()
        topBar { _, _ ->
            Box(Modifier.fillMaxWidth().semantics { isTraversalGroup = true }) {
                Box(Modifier.align(Alignment.TopEnd).height(56.dp)) {
                    DropdownMenu(
                        offset = DpOffset((-12).dp, 0.dp),
                        expanded = dropdownMenuExpanded,
                        onDismissRequest = { dropdownMenuExpanded = false }) {
                        uiState.searchTypeNameList.forEach {
                            DropdownMenuItem(
                                text = { Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.W400
                                ) },
                                onClick = { onChangeSearchType(it) }
                            )
                        }
                    }
                }
                SearchBar(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(horizontal = if (!searchBarExpanded) 12.dp else 0.dp)
                        .onGloballyPositioned { coordinates ->
                            searchBarRect = coordinates.boundsInParent()
                        }
                        .semantics { traversalIndex = 0f },
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchKeyword,
                            onQueryChange = { searchKeyword = it },
                            onSearch = {
                                searchBarExpanded = false
                                onSearch(it)
                            },
                            expanded = searchBarExpanded,
                            onExpandedChange = { searchBarExpanded = it },
                            placeholder = { AnimatedText(
                                text = uiState.searchTip,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.W400,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            ) },
                            leadingIcon = {
                                if (searchBarExpanded) {
                                    IconButton(onClick = onCLickBack) {
                                        Icon(painter = painterResource(R.drawable.arrow_back_24px), contentDescription = "back")
                                    }
                                } else IconButton(onClick = onCLickBack) {
                                    Icon(painter = painterResource(R.drawable.arrow_back_24px), contentDescription = "back")
                                }
                            },
                            trailingIcon = {
                                if (searchBarExpanded) {
                                    IconButton(onClick = { dropdownMenuExpanded = true }) {
                                        Icon(painter = painterResource(R.drawable.menu_open_24px), contentDescription = "menu")
                                    }
                                }
                            },
                        )
                    },
                    expanded = searchBarExpanded,
                    onExpandedChange = { searchBarExpanded = it },
                    windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    AnimatedVisibility(
                        visible = uiState.historyList.isEmpty() || uiState.historyList.all { it.isEmpty() },
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        EmptyPage(
                            painter = painterResource(R.drawable.schedule_90dp),
                            title = "没有内容",
                            description = "这里将保存最近的搜索结果\n" +
                                    "单击搜索栏左侧图标，并指定搜索范围"
                        )
                    }
                    AnimatedVisibility(
                        visible = uiState.historyList.isNotEmpty() || !uiState.historyList.any { it.isEmpty() },
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    modifier = Modifier.padding(16.dp, 8.dp),
                                    text = "搜索历史",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W700,
                                    lineHeight = 16.sp,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(Modifier.weight(2f))
                                Text(
                                    modifier = Modifier
                                        .padding(16.dp, 8.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = onClickClearAllHistory
                                        ),
                                    text = "清除全部",
                                    style = MaterialTheme.typography.displayLarge,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W700,
                                    lineHeight = 16.sp,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Box(Modifier.height(8.dp))
                            uiState.historyList.forEach { history ->
                                if (history.isEmpty()) return@forEach
                                AnimatedContent(
                                    targetState = history,
                                    label = "HistoryItemAnime"
                                ) {
                                    Row (
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(46.dp)
                                            .padding(horizontal = 16.dp )
                                            .clickable {
                                                searchKeyword = history
                                                searchBarExpanded = false
                                                onSearch.invoke(history)
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = history,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.W400,
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
                }
            }
        }
    }
    AnimatedVisibility(
        visible = uiState.isLoading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Loading()
    }
    AnimatedVisibility(
        visible = uiState.isLoadingComplete && uiState.searchResult.isEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        EmptyPage(
            painter = painterResource(R.drawable.not_found_90dp),
            title = "未找到类容",
            description = "可以使用较短的关键词搜索, 并检查关键词正确性"
        )
    }
    AnimatedVisibility(
        visible = !uiState.isLoading && uiState.searchResult.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 3.dp)
        ) {
            item {
                AnimatedText(
                    modifier = Modifier.padding(16.dp, 8.dp),
                    text = "\"${searchKeyword}\" 的搜索结果 (${uiState.searchResult.size}${ if (uiState.isLoadingComplete) "" else "..." })",
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W700,
                    lineHeight = 16.sp,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(uiState.searchResult) { bookInformation ->
                AnimatedContent(
                    targetState = bookInformation,
                    transitionSpec = {
                        (expandVertically(expandFrom = Alignment.Top)).togetherWith(
                            shrinkVertically(shrinkTowards = Alignment.Top)
                        )
                    },
                    label = "BookCardAnime",
                ) {
                    BookCard(
                        bookInformation = bookInformation,
                        onClickBook = onClickBook
                    )
                }
            }
            item {AnimatedVisibility(
                visible = !uiState.isLoadingComplete,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun BookCard(
    bookInformation: BookInformation,
    onClickBook: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .height(125.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClickBook(bookInformation.id)
            }
    ) {
        Cover(
            width = 82.dp,
            height = 125.dp,
            url = bookInformation.coverUrl,
            rounded = 8.dp
        )
        Column (
            modifier = Modifier.fillMaxWidth().padding(8.dp, 5.dp, 14.dp, 5.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = bookInformation.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.W700,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    maxLines = 2
                )
                Box(modifier = Modifier.weight(2f))
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_bookmark_24px),
                        contentDescription = "mark"
                    )
                }
            }
            Text(
                text =
                "作者: ${bookInformation.author} / 文库: ${bookInformation.publishingHouse}\n" +
                        "更新: ${bookInformation.lastUpdated.year}-${bookInformation.lastUpdated.month}-${bookInformation.lastUpdated.dayOfMonth}" +
                        " / 数字: ${bookInformation.wordCount}" +
                        " / ${if (bookInformation.isComplete) "已完结" else "连载中"}\n" +
                        "简介: ${bookInformation.description}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.W700,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}