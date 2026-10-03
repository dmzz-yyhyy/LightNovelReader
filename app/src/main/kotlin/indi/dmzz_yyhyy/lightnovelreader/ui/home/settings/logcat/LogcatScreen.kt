package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LogEntry
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LogLevel
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogcatScreen(
    uiState: LogcatUiState,
    onClickBack: () -> Unit,
) {
    val selectedFile = uiState.selectedLogFile
    val logFiles = uiState.logFiles
    val liveEntries = uiState.liveEntries
    val fileEntries = uiState.fileEntries

    LaunchedEffect(uiState.liveLazyListState) {
        var wasUserScrolling = false
        snapshotFlow { uiState.liveLazyListState.isScrollInProgress to uiState.liveLazyListState.canScrollForward }
            .collect { (scrolling, canScrollForward) ->
                if (scrolling && !uiState.scrollingToBottom) {
                    wasUserScrolling = true
                    uiState.stickToBottom = !canScrollForward
                } else if (!scrolling && wasUserScrolling) {
                    uiState.stickToBottom = !canScrollForward
                    wasUserScrolling = false
                }
            }
    }
    LaunchedEffect(liveEntries.lastOrNull()?.id, uiState.autoScrollEnabled, uiState.tabIndex) {
        if (uiState.tabIndex == 0 && (uiState.autoScrollEnabled || uiState.stickToBottom) && liveEntries.isNotEmpty()) {
            uiState.scrollingToBottom = true
            try {
                uiState.liveLazyListState.scrollToItem(liveEntries.lastIndex)
            } finally {
                uiState.scrollingToBottom = false
            }
        }
    }
    LaunchedEffect(selectedFile) {
        uiState.fileLazyListState.scrollToItem(0)
    }
    LaunchedEffect(uiState.tabIndex, logFiles.isNotEmpty()) {
        uiState.selectorVisible = uiState.tabIndex == 1 && logFiles.isNotEmpty()
        if (!uiState.selectorVisible) uiState.fileMenuExpanded = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        TopBar(
            uiState = uiState,
            onClickBack = onClickBack
        )
        PrimaryTabRow(
            selectedTabIndex = uiState.tabIndex,
            indicator = {
                SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(uiState.tabIndex, matchContentSize = true)
                        .height(4.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        ) {
            Tab(
                selected = uiState.tabIndex == 0,
                onClick = { uiState.tabIndex = 0 },
                text = { Text(stringResource(R.string.log_tab_live)) }
            )
            Tab(
                selected = uiState.tabIndex == 1,
                onClick = { uiState.refreshFiles(); uiState.tabIndex = 1 },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.log_tab_files))
                        AnimatedVisibility(
                            visible = logFiles.isNotEmpty(),
                        ) {
                            Badge(
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text(logFiles.size.toString())
                            }
                        }
                    }
                }
            )
        }
        if (uiState.tabIndex == 0) {
            LogEntries(liveEntries, uiState.liveLazyListState, uiState.wrapText, uiState.textSize) { uiState.textSize = it }
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                AnimatedVisibility(
                    visible = uiState.selectorVisible && logFiles.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val menuWidth = maxWidth - 36.dp
                        ExposedDropdownMenuBox(
                            expanded = uiState.fileMenuExpanded,
                            onExpandedChange = { uiState.fileMenuExpanded = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = selectedFile.orEmpty(),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(stringResource(R.string.log_source)) },
                                placeholder = { Text(stringResource(R.string.log_select_file)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.fileMenuExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            )
                            DropdownMenu(
                                expanded = uiState.fileMenuExpanded,
                                onDismissRequest = { uiState.fileMenuExpanded = false },
                                modifier = Modifier.width(menuWidth)
                            ) {
                                logFiles.forEach { fileName ->
                                    val description = when {
                                    fileName.startsWith("lnr_export_") -> stringResource(R.string.log_shared)
                                    fileName.startsWith("lnr_panic_") -> stringResource(R.string.log_crash)
                                    else -> ""
                                }
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(fileName, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                if (description.isNotEmpty()) {
                                                    Text(description, style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                        },
                                        onClick = {
                                            uiState.selectLogFile(fileName)
                                            uiState.fileMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Box(Modifier.weight(1f)) {
                    if (selectedFile == null || selectedFile !in logFiles) {
                        EmptyPage(
                            icon = painterResource(R.drawable.bug_report_24px),
                            title = stringResource(R.string.log_no_log_files)
                        )
                    } else {
                        key(selectedFile) {
                            LogEntries(fileEntries, uiState.fileLazyListState, uiState.wrapText, uiState.textSize) { uiState.textSize = it }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuSwitch(label: String, checked: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = null)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}


@Composable
private fun LogEntries(
    entries: List<LogEntry>,
    listState: LazyListState,
    wrapText: Boolean,
    textSize: Float,
    onTextSizeChange: (Float) -> Unit
) {
    if (entries.isEmpty()) {
        EmptyPage(
            icon = painterResource(R.drawable.bug_report_24px),
            title = stringResource(R.string.log_empty_list)
        )
        return
    }
    val currentTextSize = rememberUpdatedState(textSize)
    val horizontalScrollState = rememberScrollState()
    val textMeasurer = rememberTextMeasurer()
    val measuredWidths = remember(entries) { mutableMapOf<String, Int>() }
    val measurementStyle = remember { TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, letterSpacing = 0.sp) }
    val widestLine = if (wrapText) 0 else entries.maxOfOrNull { entry ->
        measuredWidths.getOrPut(entry.text) {
            textMeasurer.measure(
                text = entry.text,
                style = measurementStyle,
                softWrap = false,
                maxLines = 1
            ).size.width
        }
    } ?: 0
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom != 1f) {
                        onTextSizeChange((currentTextSize.value * zoom).coerceIn(6f, 14f))
                    }
                }
            }
    ) {
        val contentWidth = with(density) { (widestLine * textSize / 12f).toDp() } + 18.dp
        val columnWidth = if (wrapText) maxWidth else maxOf(maxWidth, contentWidth)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (wrapText) Modifier else Modifier.horizontalScroll(horizontalScrollState)
                )
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .width(columnWidth)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                items(entries, key = { it.id }) { logEntry ->
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = logEntry.text,
                        color = colorOf(logEntry.level),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.sp,
                        lineHeight = (textSize * 1.25f).sp,
                        fontSize = textSize.sp,
                        softWrap = wrapText,
                        maxLines = if (wrapText) Int.MAX_VALUE else 1
                    )
                }
            }
        }
    }
}

@Composable
private fun colorOf(level: LogLevel): Color = when (level) {
    LogLevel.ERROR -> MaterialTheme.colorScheme.error
    LogLevel.WARNING -> Color(0xFFF7B400)
    LogLevel.INFO -> MaterialTheme.colorScheme.onSurface
    LogLevel.DEBUG, LogLevel.VERBOSE -> MaterialTheme.colorScheme.outline
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    uiState: LogcatUiState,
    onClickBack: () -> Unit,
) {
    val selectedFile = uiState.selectedLogFile
    val logFiles = uiState.logFiles
    val liveEntries = uiState.liveEntries
    TopAppBar(
        title = { Text(stringResource(R.string.logs_title), style = MaterialTheme.typography.displayLarge) },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(painterResource(R.drawable.arrow_back_24px), contentDescription = "back")
            }
        },
        actions = {
            if (uiState.tabIndex == 0) {
                if (liveEntries.isNotEmpty()) {
                    IconButton(onClick = uiState::clearLiveLogs) {
                        Icon(painterResource(R.drawable.delete_forever_24px), contentDescription = "clear")
                    }
                    IconButton(onClick = uiState::shareLiveLogs) {
                        Icon(painterResource(R.drawable.ios_share_24px), contentDescription = "share")
                    }
                }
            } else if (selectedFile != null && selectedFile in logFiles) {
                IconButton(onClick = uiState::deleteSelectedFile) {
                    Icon(painterResource(R.drawable.delete_forever_24px), contentDescription = "delete")
                }
                IconButton(onClick = uiState::shareSelectedFile) {
                    Icon(painterResource(R.drawable.ios_share_24px), contentDescription = "share")
                }
            }
            Box {
                IconButton(onClick = { uiState.menuExpanded = true }) {
                    Icon(painterResource(R.drawable.more_vert_24px), contentDescription = "more")
                }
                DropdownMenu(
                    expanded = uiState.menuExpanded,
                    onDismissRequest = { uiState.menuExpanded = false }
                ) {
                    if (uiState.tabIndex == 1) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.log_clear)) },
                            onClick = { uiState.deleteAllFiles(); uiState.menuExpanded = false },
                            enabled = logFiles.isNotEmpty()
                        )
                    } else {
                        DropdownMenuItem(
                            text = { MenuSwitch(stringResource(R.string.auto_scroll), uiState.autoScrollEnabled) },
                            onClick = {
                                uiState.autoScrollEnabled = !uiState.autoScrollEnabled
                                uiState.menuExpanded = false
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { MenuSwitch(stringResource(R.string.word_wrap), uiState.wrapText) },
                        onClick = { uiState.wrapText = !uiState.wrapText; uiState.menuExpanded = false }
                    )
                }
            }
        }
    )
}
