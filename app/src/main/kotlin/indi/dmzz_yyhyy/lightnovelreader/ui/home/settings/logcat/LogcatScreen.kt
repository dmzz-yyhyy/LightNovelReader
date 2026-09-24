package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.logcat

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LogEntry
import indi.dmzz_yyhyy.lightnovelreader.data.logging.LogLevel
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedTextLine
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun LogcatScreen(
    uiState: LogcatUiState,
    logFiles: List<String>,
    logEntries: List<LogEntry>,
    onClickBack: () -> Unit,
    onClickClearLogs: () -> Unit,
    onClickShareLogs: () -> Unit,
    onClickDeleteLogFile: (String) -> Unit,
    onSelectLogFile: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var unwrapLogsText by remember { mutableStateOf(false) }
    var autoScrollEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(logEntries.size) {
        if (logEntries.size > 1 && autoScrollEnabled) {
            coroutineScope.launch {
                listState.animateScrollToItem(logEntries.size - 1)
            }
        }
    }

    Column {
        TopBar(
            uiState = uiState,
            logEntries = logEntries,
            onClickBack = onClickBack,
            onClickClearLogs = onClickClearLogs,
            onClickShareLogs = onClickShareLogs
        )

        Box(modifier = Modifier.weight(1f)) {
            if (logEntries.isEmpty()) EmptyLogListContent()
            else if (unwrapLogsText) LogListContent(logEntries, listState)
            else UnWrapLogListContent(logEntries, listState)
        }

        BottomBar(
            uiState = uiState,
            logFiles = logFiles,
            autoScrollEnabled = autoScrollEnabled,
            unwrapLogsText = unwrapLogsText,
            onSelectLogFile = onSelectLogFile,
            onClickDeleteLogFile = onClickDeleteLogFile,
            onToggleAutoScroll = { autoScrollEnabled = it },
            onToggleWrap = { unwrapLogsText = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    uiState: LogcatUiState,
    logEntries: List<LogEntry>,
    onClickBack: () -> Unit,
    onClickClearLogs: () -> Unit,
    onClickShareLogs: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = stringResource(R.string.logs_title),
                    style = MaterialTheme.typography.displayLarge
                )
                AnimatedTextLine(
                    text = uiState.selectedLogFile,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            IconButton(onClickBack) {
                Icon(
                    painterResource(id = R.drawable.arrow_back_24px),
                    contentDescription = "back"
                )
            }
        },
        actions = {
            if (logEntries.isNotEmpty()) {
                if (!uiState.isFileMode) {
                    IconButton(onClickClearLogs) {
                        Icon(
                            painterResource(id = R.drawable.delete_forever_24px),
                            contentDescription = "clear"
                        )
                    }
                }
                IconButton(onClickShareLogs) {
                    Icon(
                        painterResource(id = R.drawable.ios_share_24px),
                        contentDescription = "share"
                    )
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomBar(
    uiState: LogcatUiState,
    logFiles: List<String>,
    autoScrollEnabled: Boolean,
    unwrapLogsText: Boolean,
    onSelectLogFile: (String) -> Unit,
    onClickDeleteLogFile: (String) -> Unit,
    onToggleAutoScroll: (Boolean) -> Unit,
    onToggleWrap: (Boolean) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(100.dp)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.weight(1f)
            ) {
                TextField(
                    value = uiState.selectedLogFile,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.log_source)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    colors = ExposedDropdownMenuDefaults.textFieldColors(),
                    modifier = Modifier
                        .menuAnchor(
                            type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                            enabled = true
                        )
                        .fillMaxWidth(),
                    maxLines = 1
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.width(320.dp)
                ) {
                    logFiles.forEach { fileName ->
                        val (label, subText) = parseFileLabel(fileName)
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(colorForFile(fileName))
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column {
                                        Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        if (subText.isNotEmpty()) {
                                            Text(
                                                subText,
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = {
                                onSelectLogFile(fileName)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(painterResource(R.drawable.more_vert_24px), contentDescription = "more")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    stringResource(R.string.log_clear),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.log_clear_desc),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        },
                        onClick = {
                            onClickDeleteLogFile(":all")
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    stringResource(R.string.auto_scroll),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(Modifier.weight(1f))
                                Switch(
                                    checked = autoScrollEnabled,
                                    onCheckedChange = onToggleAutoScroll
                                )
                            }
                        },
                        onClick = {}
                    )
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    stringResource(R.string.word_wrap),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(Modifier.weight(1f))
                                Switch(checked = unwrapLogsText, onCheckedChange = onToggleWrap)
                            }
                        },
                        onClick = {}
                    )
                }
            }
        }
    }
}


@Composable
private fun parseFileLabel(fileName: String): Pair<String, String> {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
    val displayFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

    val (prefix, rawTimestamp) = when {
        fileName.startsWith("lnr_export_") -> stringResource(R.string.log_shared) to fileName.removePrefix(
            "lnr_export_"
        ).removeSuffix(".log")

        fileName.startsWith("lnr_panic_") -> stringResource(R.string.log_crash) to fileName.removePrefix(
            "lnr_panic_"
        ).removeSuffix(".log")

        else -> null to null
    }

    val timestamp = try {
        rawTimestamp?.let {
            val parsed = LocalDateTime.parse(it, formatter)
            parsed.format(displayFormatter)
        }
    } catch (_: Exception) {
        null
    }
    val subLabel = if (prefix != null && timestamp != null) "$prefix - $timestamp" else ""
    return fileName to subLabel
}

@Composable
private fun colorForFile(fileName: String): Color {
    return when {
        fileName == "实时" -> Color(0xFF4CAF50)
        fileName.startsWith("lnr_panic_") -> Color(0xFFF44336)
        fileName.startsWith("lnr_export_") -> Color(0xFF2196F3)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
fun EmptyLogListContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(44.dp),
                painter = painterResource(R.drawable.bug_report_24px),
                tint = MaterialTheme.colorScheme.outline,
                contentDescription = "empty_list_icon"
            )
            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.log_empty_list))
        }
    }
}

@Composable
fun UnWrapLogListContent(
    logEntries: List<LogEntry>,
    listState: LazyListState
) {
    Box(modifier = Modifier.width(Int.MAX_VALUE.dp)) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState()),
            contentPadding = PaddingValues(2.dp)
        ) {
            logEntries.forEach {
                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = it.text,
                        color = colorOf(it.logLevel),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.sp,
                        lineHeight = 15.sp,
                        fontSize = 12.sp,
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun colorOf(logLevel: LogLevel): Color {
    return when (logLevel.level) {
        2 -> MaterialTheme.colorScheme.error
        4 -> Color(0xFFF7B400)
        6 -> MaterialTheme.colorScheme.onSurface
        8 -> MaterialTheme.colorScheme.outline
        10 -> MaterialTheme.colorScheme.outlineVariant
        else -> MaterialTheme.colorScheme.outline
    }
}

@Composable
fun LogListContent(
    logEntries: List<LogEntry>,
    listState: LazyListState
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        contentPadding = PaddingValues(2.dp)
    ) {

        logEntries.forEach {
            item {
                Text(
                    text = it.text,
                    color = colorOf(it.logLevel),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.sp,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    softWrap = true
                )
            }
        }
    }
}