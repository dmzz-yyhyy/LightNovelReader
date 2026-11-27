package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalAppTheme
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedTextLine
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreScreen
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.ExploreUiState
import indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.PagedBookList
import io.nightfish.lightnovelreader.api.identifier.Identifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExploreSearchScreen(
    exploreUiState: ExploreUiState,
    viewModel: ExploreSearchViewModel,
    checkConnection: () -> Unit,
    onBack: () -> Unit,
    onOpenBook: (Identifier.Book) -> Unit,
    addToBookshelf: (Identifier.Book) -> Unit
) {
    val colorScheme = LocalAppTheme.current.colorScheme
    val page by viewModel.pageState.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current
    val focus = LocalFocusManager.current
    val inputFocus = remember { FocusRequester() }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var requestEditorFocus by remember { mutableStateOf(false) }

    fun submit(value: String) {
        if (value.isBlank()) return
        focus.clearFocus()
        keyboard?.hide()
        viewModel.search(value)
    }

    fun openSettings() {
        focus.clearFocus()
        keyboard?.hide()
        showSettings = true
    }

    LaunchedEffect(page.openBook) {
        page.openBook?.let {
            viewModel.editSearch()
            onOpenBook(it)
        }
    }

    ExploreScreen(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        refresh = checkConnection,
        uiState = exploreUiState
    ) {
        AnimatedContent(
            targetState = viewModel.submittedKeyword,
            modifier = Modifier.fillMaxSize().navigationBarsPadding().imePadding(),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            contentKey = { it != null },
            label = "Search screen"
        ) { submitted ->
            if (submitted == null) {
                Box(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize()) {
                        LaunchedEffect(requestEditorFocus) {
                            if (requestEditorFocus) {
                                inputFocus.requestFocus()
                                requestEditorFocus = false
                            }
                        }
                        val tip = viewModel.getSearchTip()
                        TextField(
                            value = viewModel.keyword,
                            onValueChange = viewModel::updateInput,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .focusRequester(inputFocus),
                            leadingIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(
                                        painterResource(R.drawable.arrow_back_24px),
                                        contentDescription = "back"
                                    )
                                }
                            },
                            trailingIcon = {
                                AnimatedVisibility(
                                    visible = viewModel.keyword.isNotEmpty(),
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    IconButton(onClick = viewModel::clearInput) {
                                        Icon(
                                            painterResource(R.drawable.close_24px),
                                            contentDescription = "clear"
                                        )
                                    }
                                }
                            },
                            placeholder = {
                                AnimatedTextLine(
                                    text = tip.ifBlank { stringResource(R.string.explore_search_hint) },
                                    color = colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surfaceContainerHigh,
                                unfocusedContainerColor = colorScheme.surfaceContainerHigh,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { submit(viewModel.keyword) })
                        )
                        SearchSuggestions(
                            input = viewModel.keyword,
                            history = viewModel.history,
                            suggestions = viewModel.suggestions,
                            onSubmit = ::submit,
                            onFill = viewModel::updateInput,
                            onDelete = viewModel::deleteHistory,
                            clearHistory = viewModel::clearHistory,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (viewModel.settingDefinitions.isNotEmpty()) {
                        FloatingActionButton(
                            onClick = ::openSettings,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.filled_settings_24px),
                                contentDescription = "filter"
                            )
                        }
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                painterResource(R.drawable.arrow_back_24px),
                                contentDescription = "back"
                            )
                        }
                        OutlinedTextField(
                            value = viewModel.keyword,
                            onValueChange = {
                                viewModel.editSearch()
                                viewModel.updateInput(it)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged {
                                    if (it.isFocused) {
                                        requestEditorFocus = true
                                        viewModel.editSearch()
                                    }
                                },
                            shape = MaterialTheme.shapes.large,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { submit(viewModel.keyword) })
                        )
                        if (viewModel.settingDefinitions.isNotEmpty()) {
                            IconButton(onClick = ::openSettings) {
                                Icon(Icons.Outlined.Settings, contentDescription = "filter")
                            }
                        }
                    }
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
                        heading = {
                            Text(
                                stringResource(R.string.explore_search_results, submitted),
                                Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                }
            }
        }
    }
    if (showSettings) {
        ModalBottomSheet(onDismissRequest = { showSettings = false }) {
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
                    setting.Content({ viewModel.settings }, viewModel::changeSettings)
                    HorizontalDivider(Modifier.padding(top = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun SearchSuggestions(
    input: String,
    history: List<String>,
    suggestions: List<String>,
    onSubmit: (String) -> Unit,
    onFill: (String) -> Unit,
    onDelete: (String) -> Unit,
    clearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var historyExpanded by rememberSaveable { mutableStateOf(true) }
    val recent = history.filter { it.contains(input, ignoreCase = true) }
    AnimatedContent(
        targetState = history.isEmpty() && (input.isBlank() || suggestions.isEmpty()),
        modifier = modifier.fillMaxWidth(),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
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
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp)
            ) {
                if (input.isBlank() && history.isNotEmpty()) {
                    item(key = "history-header") {
                        Row(
                            Modifier.fillMaxWidth().animateItem(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.search_history),
                                Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium
                            )
                            TextButton(onClick = clearHistory) {
                                Text(stringResource(R.string.clear_all))
                            }
                            val rotation by animateFloatAsState(
                                targetValue = if (historyExpanded) 180f else 0f,
                                label = "Search history arrow"
                            )
                            IconButton(onClick = { historyExpanded = !historyExpanded }) {
                                val description = if (historyExpanded) {
                                    R.string.explore_collapse
                                } else {
                                    R.string.explore_expand
                                }
                                Icon(
                                    painterResource(R.drawable.keyboard_arrow_up_24px),
                                    contentDescription = stringResource(description),
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
                                Icon(painterResource(R.drawable.close_24px), contentDescription = "delete")
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
                                painterResource(R.drawable.arrow_outward_24px),
                                contentDescription = stringResource(R.string.explore_fill_search),
                                modifier = Modifier.rotate(-90f)
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
            Modifier
                .fillMaxWidth()
                .clickable { onClick(value) },
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(icon),
                contentDescription = null,
                tint = LocalAppTheme.current.colorScheme.onSurfaceVariant
            )
            Text(value, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            trailing()
        }
        HorizontalDivider()
    }
}
