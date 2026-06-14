package indi.dmzz_yyhyy.lightnovelreader.ui.bookmanager

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.download.DownloadItem
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Cover
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.data.MenuOptions
import indi.dmzz_yyhyy.lightnovelreader.utils.formTime
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import io.nightfish.lightnovelreader.api.book.BookInformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookManagerScreen(
    onClickBack: () -> Unit,
    downloadItemIdList: List<DownloadItem>,
    bookInformationMap: Map<String, BookInformation>,
    uiState: LocalBookManagerUiState,
    loadBookInfo: (String) -> Unit,
    onClickCancel: (DownloadItem) -> Unit,
    onClickClearCompleted: () -> Unit
) {
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var moreMenuExpanded by remember { mutableStateOf(false) }
    var orphanDialogVisible by remember { mutableStateOf(false) }
    val appBarColor by animateColorAsState(
        targetValue =
            if (uiState.isSelecting && tabIndex == 1) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface,
        animationSpec = tween(80)
    )
    BackHandler(enabled = tabIndex == 1 && uiState.isSelecting) {
        uiState.exitSelection()
    }
    if (orphanDialogVisible) {
        AlertDialog(
            onDismissRequest = { orphanDialogVisible = false },
            title = { Text(stringResource(R.string.book_manager_clear_orphaned_title)) },
            text = { Text(stringResource(R.string.book_manager_clear_orphaned_content)) },
            dismissButton = {
                TextButton(onClick = { orphanDialogVisible = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        orphanDialogVisible = false
                        uiState.clearOrphanedData()
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
    Scaffold(
        topBar = {
            if (tabIndex == 1 && uiState.isSelecting) {
                SelectingAppBar(
                    uiState = uiState,
                    backgroundColor = appBarColor
                )
            } else {
                AppBar(
                    onClickBack = onClickBack,
                    tabIndex = tabIndex,
                    sortMenuExpanded = sortMenuExpanded,
                    onSortMenuExpandedChange = { sortMenuExpanded = it },
                    moreMenuExpanded = moreMenuExpanded,
                    onMoreMenuExpandedChange = { moreMenuExpanded = it },
                    onOpenOrphanDialog = { orphanDialogVisible = true },
                    uiState = uiState,
                    backgroundColor = appBarColor
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            PrimaryTabRow(
                selectedTabIndex = tabIndex,
                indicator = {
                    SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(
                                selectedTabIndex = tabIndex,
                                matchContentSize = true
                            )
                            .height(4.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            ) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = { Text(stringResource(R.string.book_manager_tab_downloads)) }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = { Text(stringResource(R.string.book_manager_tab_local_books)) }
                )
            }
            Box {
                if (tabIndex == 0) {
                    DownloadManagerContent(
                        downloadItemIdList = downloadItemIdList,
                        bookInformationMap = bookInformationMap,
                        loadBookInfo = loadBookInfo,
                        onClickCancel = onClickCancel,
                        onClickClearCompleted = onClickClearCompleted
                    )
                } else {
                    LocalBookManagerContent(
                        uiState = uiState
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    onClickBack: () -> Unit,
    tabIndex: Int,
    sortMenuExpanded: Boolean,
    onSortMenuExpandedChange: (Boolean) -> Unit,
    moreMenuExpanded: Boolean,
    onMoreMenuExpandedChange: (Boolean) -> Unit,
    onOpenOrphanDialog: () -> Unit,
    uiState: LocalBookManagerUiState,
    backgroundColor: Color
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            scrolledContainerColor = backgroundColor
        ),
        title = {
            Text(
                text = stringResource(R.string.book_manager_title),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.W600
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
            if (tabIndex == 1) {
                Box {
                    IconButton(
                        enabled = uiState.bookList.isNotEmpty(),
                        onClick = { onSortMenuExpandedChange(true) }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.filter_list_24px),
                            contentDescription = "sort"
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { onSortMenuExpandedChange(false) }
                    ) {
                        MenuOptions.BookshelfSortTypeOptions.optionList.forEach { option ->
                            val sort = LocalBookSort.valueOf(option.key)

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = uiState.sort == sort,
                                            onClick = null
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(stringResource(option.nameId))
                                    }
                                },
                                onClick = {
                                    uiState.setSort(sort)
                                    onSortMenuExpandedChange(false)
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = uiState.sortReverse,
                                        onCheckedChange = null
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.book_manager_sort_reverse))
                                }
                            },
                            onClick = {
                                uiState.setReverse(!uiState.sortReverse)
                            }
                        )
                    }
                }

                IconButton(
                    enabled = uiState.bookList.isNotEmpty(),
                    onClick = { uiState.enterSelection(null) }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.checklist_24px),
                        contentDescription = "select"
                    )
                }
                Box {
                    IconButton(
                        enabled = uiState.bookList.isNotEmpty(),
                        onClick = { onMoreMenuExpandedChange(true) }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.more_vert_24px),
                            contentDescription = "more"
                        )
                    }
                    DropdownMenu(
                        expanded = moreMenuExpanded,
                        onDismissRequest = { onMoreMenuExpandedChange(false) }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.book_manager_clear_orphaned)) },
                            onClick = {
                                onMoreMenuExpandedChange(false)
                                onOpenOrphanDialog()
                            }
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectingAppBar(
    uiState: LocalBookManagerUiState,
    backgroundColor: Color
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            scrolledContainerColor = backgroundColor
        ),
        title = {
            Text(
                text = stringResource(R.string.book_manager_selected_count, uiState.selectedIds.size),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.W600
            )
        },
        navigationIcon = {
            IconButton(onClick = uiState.exitSelection) {
                Icon(
                    painter = painterResource(id = R.drawable.close_24px),
                    contentDescription = "close"
                )
            }
        }
    )
}

@Composable
private fun DownloadManagerContent(
    downloadItemIdList: List<DownloadItem>,
    bookInformationMap: Map<String, BookInformation>,
    loadBookInfo: (String) -> Unit,
    onClickCancel: (DownloadItem) -> Unit,
    onClickClearCompleted: () -> Unit
) {
    val itemList = downloadItemIdList.distinctBy { it.type to it.bookId }
    if (itemList.isEmpty()) {
        EmptyPage(
            modifier = Modifier.navigationBarsPadding(),
            icon = painterResource(id = R.drawable.download_24px),
            title = stringResource(R.string.nothing_here),
            description = stringResource(R.string.nothing_here_desc_book_manager),
        )
        return
    }
    LazyColumn(
        modifier = Modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (itemList.any { it.progress < 1f })
            item {
                Text(
                    modifier = Modifier.height(34.dp).animateItem(),
                    text = stringResource(R.string.download_in_progress),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.W600
                )
            }
        items(
            items = itemList.filter { it.progress < 1f }.reversed(),
            key = { "${it.type.name}_${it.bookId}" }
        ) { downloadItem ->
            val bookInformation = bookInformationMap[downloadItem.bookId] ?: BookInformation.empty(downloadItem.bookId)
            LaunchedEffect(downloadItem.bookId) {
                loadBookInfo(downloadItem.bookId)
            }
            Card(
                modifier = Modifier.animateItem(),
                bookInformation = bookInformation,
                downloadItem = downloadItem,
                onClickCancel = { onClickCancel(downloadItem) }
            )
        }
        if (itemList.any { it.progress >= 1f })
            item {
                Row(
                    modifier = Modifier.animateItem(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.completed),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.W600
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClickClearCompleted) {
                        Text(
                            text = stringResource(R.string.clear_all),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.W600,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        items(
            items = itemList.filter { it.progress >= 1f }.reversed(),
            key = { "${it.type.name}_${it.bookId}" }
        ) { downloadItem ->
            val bookInformation = bookInformationMap[downloadItem.bookId] ?: BookInformation.empty(downloadItem.bookId)
            LaunchedEffect(downloadItem.bookId) {
                loadBookInfo(downloadItem.bookId)
            }
            Card(
                modifier = Modifier.animateItem(),
                bookInformation = bookInformation,
                downloadItem = downloadItem,
                onClickCancel = { onClickCancel(downloadItem) }
            )
        }
        navigationBarSpacer()
    }
}

@Composable
private fun Card(
    modifier: Modifier = Modifier,
    bookInformation: BookInformation,
    downloadItem: DownloadItem,
    onClickCancel: () -> Unit
) {
    val progressAnim by animateFloatAsState(
        targetValue = downloadItem.progress,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "",
    )
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Cover(
            width = 64.dp,
            height = 93.dp,
            uri = bookInformation.coverUri
        )
        Box(Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = bookInformation.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.W600
            )
            Text(
                text = bookInformation.author,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.W500,
                letterSpacing = 0.15.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp),
                    painter =
                        if (downloadItem.progress >= 1) painterResource(R.drawable.done_outline_24px)
                        else if (downloadItem.progress >= 0) painterResource(downloadItem.type.icon)
                        else painterResource(R.drawable.error_24px),
                    contentDescription = null
                )
                Box(Modifier.width(10.dp))
                Text(
                    text =
                        if (downloadItem.progress < 1)
                            stringResource(R.string.download_item_progress,
                                formTime(downloadItem.startTime),
                                (downloadItem.progress*100).toInt()
                            )
                        else if (downloadItem.progress > 0)
                            stringResource(R.string.download_item_finished, downloadItem.type.typeName)
                        else stringResource(R.string.download_item_failed, downloadItem.type.typeName),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.W500,
                    letterSpacing = 0.15.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (downloadItem.progress < 1)
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { progressAnim },
                )
        }
        if (downloadItem.progress < 1)
            IconButton(onClickCancel) {
                Icon(
                    painter = painterResource(R.drawable.cancel_24px),
                    contentDescription = "cancel"
                )
            }
        Box(Modifier.width(7.dp))
    }
}
