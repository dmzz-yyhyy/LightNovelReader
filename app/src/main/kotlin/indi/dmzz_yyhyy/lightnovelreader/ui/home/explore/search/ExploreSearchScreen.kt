package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.withResumed
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SectionHeader
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.PagedBookList
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.coroutines.launch

@Composable
internal fun ExploreSearchScreen(
    exploreUiState: ExploreUiState,
    viewModel: ExploreSearchViewModel,
    isActive: Boolean,
    checkConnection: () -> Unit,
    onBack: () -> Unit,
    onOpenBook: (Identifier.Book) -> Unit,
    addToBookshelf: (Identifier.Book) -> Unit
) {
    val page by viewModel.pageState.collectAsStateWithLifecycle()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val resultKeyword = viewModel.submittedKeyword
    val input = rememberTextFieldState(initialText = resultKeyword.orEmpty())
    val initialSearchBarValue = remember {
        if (resultKeyword == null) SearchBarValue.Expanded else SearchBarValue.Collapsed
    }
    val searchBarState = rememberSearchBarState(initialValue = initialSearchBarValue)
    val expanded = searchBarState.targetValue == SearchBarValue.Expanded
    val searchBarCollapsed = searchBarState.currentValue == SearchBarValue.Collapsed
    val scope = rememberCoroutineScope()
    val resultListState = rememberSaveableStateHolder()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val hasSettings = viewModel.settingDefinitions.isNotEmpty()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var historyExpanded by rememberSaveable { mutableStateOf(true) }

    fun collapseSearch(query: String) {
        focusManager.clearFocus()
        keyboard?.hide()
        if (input.text.toString() != query) input.setTextAndPlaceCursorAtEnd(query)
        scope.launch { searchBarState.animateToCollapsed() }
    }

    fun submit(value: String) {
        val query = value.trim()
        if (!isActive || query.isEmpty()) return
        viewModel.search(query)
        collapseSearch(query)
    }

    fun openSettings() {
        keyboard?.hide()
        showSettings = true
    }

    BackHandler(enabled = isActive && expanded && resultKeyword != null && !exploreUiState.isOffLine) {
        viewModel.submittedKeyword?.let(::collapseSearch)
    }

    LaunchedEffect(input) {
        snapshotFlow { input.text.toString() }.collect { viewModel.updateSuggestions(it) }
    }
    LaunchedEffect(isActive, resultKeyword, searchBarState) {
        if (isActive && resultKeyword == null && searchBarState.currentValue == SearchBarValue.Collapsed) {
            searchBarState.animateToExpanded()
        }
    }
    LaunchedEffect(isActive, expanded, exploreUiState.isOffLine, lifecycle) {
        if (isActive && expanded && !exploreUiState.isOffLine) {
            lifecycle.withResumed {
                focusRequester.requestFocus()
                keyboard?.show()
            }
        }
    }
    LaunchedEffect(page.openBook, isActive, expanded, searchBarCollapsed, lifecycle) {
        if (isActive && !expanded && searchBarCollapsed) {
            page.openBook?.let { book ->
                lifecycle.withResumed {
                    viewModel.returnToSearch()
                    onOpenBook(book)
                }
            }
        }
    }
    val outerHorizontal by animateDpAsState(if (expanded) 0.dp else 16.dp, label = "Search outer horizontal")
    val outerVertical by animateDpAsState(if (expanded) 0.dp else 8.dp, label = "Search outer vertical")
    val searchBarColors = SearchBarDefaults.colors()

    ExploreScreen(
        modifier = Modifier.fillMaxSize(),
        refresh = checkConnection,
        uiState = exploreUiState
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = if (expanded) searchBarColors.containerColor else MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = outerHorizontal)
                        .padding(bottom = outerVertical)
                ) {
                    val inputField: @Composable () -> Unit = {
                        val tip = viewModel.getSearchTip()
                        SearchBarDefaults.InputField(
                            textFieldState = input,
                            searchBarState = searchBarState,
                            onSearch = ::submit,
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            leadingIcon = { SearchBackButton(onBack) },
                            trailingIcon = when {
                                expanded && input.text.isNotEmpty() -> {
                                    {
                                        IconButton(onClick = { input.setTextAndPlaceCursorAtEnd("") }) {
                                            Icon(
                                                painterResource(R.drawable.close_24px),
                                                contentDescription = "clear"
                                            )
                                        }
                                    }
                                }
                                !expanded && hasSettings -> {
                                    {
                                        IconButton(onClick = ::openSettings) {
                                            Icon(Icons.Outlined.Settings, contentDescription = "filter")
                                        }
                                    }
                                }
                                else -> null
                            },
                            placeholder = {
                                Text(
                                    text = tip.ifBlank { stringResource(R.string.explore_search_hint) },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                    if (expanded) {
                        inputField()
                    } else {
                        SearchBar(
                            state = searchBarState,
                            inputField = inputField,
                            modifier = Modifier.fillMaxWidth(),
                            colors = searchBarColors
                        )
                    }
                }
            }
        ) { padding ->
            if (expanded) {
                Column(Modifier.fillMaxSize().padding(padding).navigationBarsPadding().imePadding()) {
                    HorizontalDivider(color = searchBarColors.dividerColor)
                    Box(Modifier.fillMaxSize()) {
                        SearchSuggestions(
                            input = input.text.toString(),
                            history = viewModel.history,
                            suggestions = viewModel.suggestions,
                            historyExpanded = historyExpanded,
                            onToggleHistory = { historyExpanded = !historyExpanded },
                            onSubmit = ::submit,
                            onFill = { input.setTextAndPlaceCursorAtEnd(it) },
                            onDelete = viewModel::deleteHistory,
                            clearHistory = viewModel::clearHistory,
                            modifier = Modifier.fillMaxSize()
                        )
                        if (hasSettings) {
                            SearchSettingsFab(::openSettings, Modifier.align(Alignment.BottomEnd))
                        }
                    }
                }
            } else if (resultKeyword != null) {
                resultListState.SaveableStateProvider("searchResults") {
                    PagedBookList(
                        state = page,
                        filters = viewModel.filterDefinitions,
                        filterValues = { viewModel.filters },
                        updateFilters = viewModel::updateFilters,
                        append = viewModel::append,
                        retry = viewModel::retry,
                        bookshelfIds = viewModel.bookshelfIds,
                        processBook = viewModel::processBook,
                        onOpenBook = onOpenBook,
                        addToBookshelf = addToBookshelf,
                        modifier = Modifier.fillMaxSize().padding(padding).navigationBarsPadding(),
                        heading = {
                            SectionHeader(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                text = stringResource(R.string.explore_search_results, resultKeyword)
                            )
                        }
                    )
                }
            }
        }
    }

    if (showSettings) {
        SearchSettingsSheet(viewModel, onDismissRequest = { showSettings = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchSettingsSheet(viewModel: ExploreSearchViewModel, onDismissRequest: () -> Unit) {
    var draft by remember { mutableStateOf(viewModel.settings) }
    ModalBottomSheet(
        onDismissRequest = {
            viewModel.changeSettings(draft)
            onDismissRequest()
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    stringResource(R.string.explore_search_settings),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(viewModel.settingDefinitions, key = { it.key.id }) { setting ->
                setting.Content({ draft }) { draft = it }
                HorizontalDivider(Modifier.padding(top = 12.dp))
            }
        }
    }
}

@Composable
private fun SearchBackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(painterResource(R.drawable.arrow_back_24px), contentDescription = "back")
    }
}

@Composable
private fun SearchSettingsFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(onClick = onClick, modifier = modifier.padding(24.dp)) {
        Icon(painterResource(R.drawable.filled_settings_24px), contentDescription = "filter")
    }
}

@Composable
private fun SearchSuggestions(
    input: String,
    history: List<String>,
    suggestions: List<String>,
    historyExpanded: Boolean,
    onToggleHistory: () -> Unit,
    onSubmit: (String) -> Unit,
    onFill: (String) -> Unit,
    onDelete: (String) -> Unit,
    clearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recent = history.filter { it.contains(input, ignoreCase = true) }
    Crossfade(
        targetState = history.isEmpty() && (input.isBlank() || suggestions.isEmpty()),
        modifier = modifier.fillMaxWidth(),
        label = "Search history empty state"
    ) { showEmptyPage ->
        if (showEmptyPage) {
            EmptyPage(
                icon = painterResource(R.drawable.schedule_90dp),
                title = stringResource(R.string.nothing_here),
                description = stringResource(R.string.nothing_here_desc_search)
            )
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                if (input.isBlank() && history.isNotEmpty()) {
                    item(key = "history-header") {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp).animateItem(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionHeader(
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.search_history),
                            )
                            TextButton(onClick = clearHistory) {
                                Text(stringResource(R.string.clear_all))
                            }
                            val rotation by animateFloatAsState(
                                targetValue = if (historyExpanded) 180f else 0f,
                                label = "Search history arrow"
                            )
                            IconButton(onClick = onToggleHistory) {
                                Icon(
                                    painterResource(R.drawable.keyboard_arrow_up_24px),
                                    contentDescription = if (historyExpanded) "collapse" else "expand",
                                    modifier = Modifier.rotate(rotation)
                                )
                            }
                        }
                    }
                }
                if (historyExpanded || input.isNotBlank()) {
                    items(recent, key = { "history:$it" }) { entry ->
                        SearchSuggestionRow(
                            entry, R.drawable.outline_schedule_24px, onSubmit, Modifier.animateItem()
                        ) {
                            IconButton(onClick = { onDelete(entry) }) {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(R.drawable.close_24px),
                                    contentDescription = "delete"
                                )
                            }
                        }
                    }
                }
                items(suggestions.filterNot { it in recent }, key = { "suggestion:$it" }) { entry ->
                    SearchSuggestionRow(
                        entry, R.drawable.search_24px, onSubmit, Modifier.animateItem()
                    ) {
                        IconButton(onClick = { onFill(entry) }) {
                            Icon(
                                modifier = Modifier.rotate(-90f).size(20.dp),
                                painter = painterResource(R.drawable.arrow_outward_24px),
                                contentDescription = "fill"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestionRow(
    value: String,
    icon: Int,
    onClick: (String) -> Unit,
    modifier: Modifier,
    trailing: @Composable () -> Unit
) {
    Column(modifier) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clickable { onClick(value) }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(icon),
                contentDescription = null,
                tint = LocalAppTheme.current.colorScheme.onSurfaceVariant
            )
            Text(value, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            trailing()
        }
        HorizontalDivider(
            Modifier.padding(horizontal = 16.dp)
        )
    }
}
