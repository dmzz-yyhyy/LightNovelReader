@file:Suppress("AssignedValueIsNeverRead")

package indi.dmzz_yyhyy.lightnovelreader.ui.home.bookshelf.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.unclippedBoundsInWindow
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.work.SaveBookshelfWork
import indi.dmzz_yyhyy.lightnovelreader.ui.components.AnimatedText
import indi.dmzz_yyhyy.lightnovelreader.ui.components.BookCardItem
import indi.dmzz_yyhyy.lightnovelreader.ui.components.EmptyPage
import indi.dmzz_yyhyy.lightnovelreader.utils.bottomBarPadding
import indi.dmzz_yyhyy.lightnovelreader.utils.bottomBarSpacer
import indi.dmzz_yyhyy.lightnovelreader.utils.navigationBarSpacer
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun BookshelfHomeScreen(
    init: () -> Unit,
    changePage: (Int) -> Unit,
    changeBookSelectState: (String) -> Unit,
    uiState: BookshelfHomeUiState,
    onClickCreate: () -> Unit,
    onClickEdit: (Int) -> Unit,
    onClickBook: (String) -> Unit,
    onClickEnableSelectMode: () -> Unit,
    onClickDisableSelectMode: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickPin: () -> Unit,
    onClickRemove: () -> Unit,
    onClickMarkSelectedBooks: () -> Unit,
    saveAllBookshelfJsonData: (Uri) -> Unit,
    saveBookshelfJsonData: (Uri) -> Unit,
    importBookshelf: (Uri) -> Unit,
    clearToast: () -> Unit,
    @Suppress("unused") sharedTransitionScope: SharedTransitionScope,
    getBookInfoFlow: (String) -> StateFlow<BookInformation>,
    getBookVolumesFlow: (String) -> StateFlow<BookVolumes>,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)
    val enterAlwaysScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val animatedBackgroundColor by animateColorAsState(
        if (!uiState.selectMode) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
    )
    val saveAllBookshelfLauncher = launcher(saveAllBookshelfJsonData)
    val saveThisBookshelfLauncher = launcher(saveBookshelfJsonData)
    val importBookshelfLauncher = launcher(importBookshelf)

    val onLongPress: (String) -> Unit = { bookId ->
        if (!uiState.selectMode) {
            onClickEnableSelectMode.invoke()
        }
        changeBookSelectState(bookId)
    }

    BackHandler(uiState.selectMode) {
        onClickDisableSelectMode()
    }

    val listState = rememberSaveable(
        saver = LazyListState.Saver
    ) {
        LazyListState()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        init()
    }

    LaunchedEffect(uiState.toast) {
        if (uiState.toast.isEmpty()) return@LaunchedEffect
        Toast.makeText(context, uiState.toast, Toast.LENGTH_SHORT).show()
        clearToast()
    }

    Column {
        TopBar(
            scrollBehavior = enterAlwaysScrollBehavior,
            backgroundColor = animatedBackgroundColor,
            selectMode = uiState.selectMode,
            uiState = uiState,
            onClickCreate = onClickCreate,
            onClickSearch = {  },
            onClickEdit = { onClickEdit(uiState.selectedBookshelfId) },
            onClickDisableSelectMode = onClickDisableSelectMode,
            onClickSelectAll = onClickSelectAll,
            onClickPin = onClickPin,
            onClickRemove = onClickRemove,
            onClickBookmark = onClickMarkSelectedBooks,
            onClickShareBookshelf = {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.applicationInfo.processName}.provider",
                    File(context.cacheDir, "LightNovelReaderBookshelfData.lnr")
                )
                val workRequest = OneTimeWorkRequestBuilder<SaveBookshelfWork>()
                    .setInputData(
                        workDataOf(
                            "bookshelfId" to uiState.selectedBookshelfId,
                            "uri" to uri.toString(),
                        )
                    )
                    .build()
                workManager.enqueueUniqueWork(
                    uri.toString(),
                    ExistingWorkPolicy.KEEP,
                    workRequest
                )
                coroutineScope.launch(Dispatchers.IO) {
                    workManager.getWorkInfoByIdFlow(workRequest.id).collect {
                        when (it?.state) {
                            WorkInfo.State.SUCCEEDED -> {
                                ShareCompat.IntentBuilder(context)
                                    .setType("application/zip")
                                    .setSubject("分享文件")
                                    .addStream(uri)
                                    .setChooserTitle("分享书架")
                                    .startChooser()
                            }

                            else -> return@collect
                        }
                    }
                }
            },
            onClickSaveThisBookshelf = {
                createBookshelfDataFile(
                    uiState.selectedBookshelf.name,
                    saveThisBookshelfLauncher
                )
            },
            onClickSaveAllBookshelf = {
                createBookshelfDataFile(
                    "bookshelves",
                    saveAllBookshelfLauncher
                )
            },
            onClickImportBookshelf = { selectBookshelfDataFile(importBookshelfLauncher) }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AnimatedVisibility(
                visible = !uiState.selectMode,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                if (uiState.bookshelfList.isNotEmpty()) {
                    val selectedIndex = uiState.selectedTabIndex
                        .takeIf { it in uiState.bookshelfList.indices }
                        ?: 0

                    PrimaryScrollableTabRow(
                        selectedTabIndex = selectedIndex,
                        edgePadding = 16.dp,
                        indicator = {
                            SecondaryIndicator(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .tabIndicatorOffset(selectedIndex)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(MaterialTheme.colorScheme.secondary),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        uiState.bookshelfList.forEach { bookshelf ->
                            Tab(
                                modifier = Modifier,
                                selected = uiState.selectedBookshelfId == bookshelf.id,
                                onClick = { if (!uiState.selectMode) changePage(bookshelf.id) },
                                text = {
                                    Text(
                                        text = bookshelf.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            )
                        }
                        }
                    }
                }

            val allBookIds = uiState.selectedBookshelf.allBookIds

            val updatedIds by remember(uiState.selectedBookshelf.updatedBookIds) {
                mutableStateOf(uiState.selectedBookshelf.updatedBookIds.reversed())
            }
            val pinnedIds by remember(uiState.selectedBookshelf.pinnedBookIds) {
                mutableStateOf(uiState.selectedBookshelf.pinnedBookIds.reversed())
            }
            val allIds by remember(uiState.selectedBookshelf.allBookIds) {
                mutableStateOf(uiState.selectedBookshelf.allBookIds.reversed())
            }

            var showEmptyPage by remember { mutableStateOf(allBookIds.isEmpty()) }

            LaunchedEffect(allBookIds) {
                if (allBookIds.isEmpty()) {
                    delay(140)
                    showEmptyPage = true
                } else {
                    showEmptyPage = false
                }
            }

            AnimatedVisibility(
                visible = showEmptyPage,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyPage(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .bottomBarPadding(),
                    icon = painterResource(R.drawable.bookmarks_90px),
                    title = stringResource(R.string.nothing_here),
                    description = stringResource(R.string.nothing_here_desc_bookshelf)
                )
            }

            val shimmerInstance = rememberShimmer(ShimmerBounds.Custom)
            val density = LocalDensity.current
            val lineHeight = MaterialTheme.typography.titleMedium.lineHeight
            val titleHeight = with(density) {
                (lineHeight * 2.2f).toDp()
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(enterAlwaysScrollBehavior.nestedScrollConnection)
                    .onGloballyPositioned { layoutCoordinates ->
                        val position = layoutCoordinates.unclippedBoundsInWindow()
                        shimmerInstance.updateBounds(position)
                    },
                state = listState
            ) {
                if (updatedIds.isNotEmpty()) {
                    stickyHeader {
                        CollapseHeader(
                            icon = painterResource(R.drawable.autorenew_24px),
                            title = stringResource(R.string.bookshelf_group_title_updated, updatedIds.size),
                            expanded = uiState.updatedExpanded,
                            onToggleExpand = { uiState.updatedExpanded = !uiState.updatedExpanded }
                        )
                    }

                    if (uiState.updatedExpanded) {
                        items(
                            items = updatedIds,
                            key = { "updated_$it"}
                        ) { id ->
                            val infoFlow = remember(id) { getBookInfoFlow(id) }
                            val info by infoFlow.collectAsStateWithLifecycle()

                            val volumesFlow = remember(id) { getBookVolumesFlow(id) }
                            val volumes by volumesFlow.collectAsStateWithLifecycle()

                            val lastChapterTitle by remember(volumes) {
                                derivedStateOf {
                                    if (volumes.volumes.isNotEmpty()) {
                                        "${volumes.volumes.last().volumeTitle} ${volumes.volumes.last().chapters.last().title}"
                                    } else null
                                }
                            }

                            BookCardItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 12.dp),
                                bookInformation = info,
                                selected = uiState.selectedBookIds.contains(id),
                                collected = false,
                                onClick = {
                                    if (!uiState.selectMode) onClickBook(id)
                                    else changeBookSelectState(id)
                                },
                                onLongPress = { onLongPress(id) },
                                latestChapterTitle = lastChapterTitle ?: uiState.bookLastChapterTitleMap[id],
                                shimmer = shimmerInstance,
                                titleHeight = titleHeight
                            )
                        }
                    }
                }

                if (pinnedIds.isNotEmpty()) {
                    stickyHeader {
                        CollapseHeader(
                            icon = painterResource(R.drawable.keep_24px),
                            title = stringResource(R.string.bookshelf_group_title_pinned, pinnedIds.size),
                            expanded = uiState.pinnedExpanded,
                            onToggleExpand = { uiState.pinnedExpanded = !uiState.pinnedExpanded }
                        )
                    }
                    if (uiState.pinnedExpanded) {
                        items(
                            items = pinnedIds,
                            key = { "pinned_$it" }
                        ) { id ->
                            val infoFlow = remember(id) { getBookInfoFlow(id) }
                            val info by infoFlow.collectAsStateWithLifecycle()

                            BookCardItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 12.dp),
                                bookInformation = info,
                                selected = uiState.selectedBookIds.contains(id),
                                collected = false,
                                onClick = {
                                    if (!uiState.selectMode) onClickBook(id)
                                    else changeBookSelectState(id)
                                },
                                onLongPress = { onLongPress(id) },
                                shimmer = shimmerInstance,
                                titleHeight = titleHeight
                            )
                        }
                    }
                }

                if (allIds.isNotEmpty()) {
                    stickyHeader {
                        CollapseHeader(
                            icon = painterResource(R.drawable.outline_bookmark_24px),
                            title = stringResource(R.string.bookshelf_group_title_all, allIds.size),
                            expanded = uiState.allExpanded,
                            onToggleExpand = { uiState.allExpanded = !uiState.allExpanded }
                        )
                    }

                    if (uiState.allExpanded) {
                        items(
                            items = allIds,
                            key = { "book_$it" }
                        ) { id ->
                            val infoFlow = remember(id) { getBookInfoFlow(id) }
                            val info by infoFlow.collectAsStateWithLifecycle()

                            BookCardItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 12.dp),
                                bookInformation = info,
                                selected = uiState.selectedBookIds.contains(id),
                                collected = false,
                                onClick = {
                                    if (!uiState.selectMode) onClickBook(id)
                                    else changeBookSelectState(id)
                                },
                                onLongPress = { onLongPress(id) },
                                shimmer = shimmerInstance,
                                titleHeight = titleHeight
                            )
                        }


                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    modifier = Modifier.padding(vertical = 18.dp),
                                    text = stringResource(R.string.n_books, allBookIds.size),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.W600,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }
                navigationBarSpacer()
                bottomBarSpacer()
            }
        }
    }
}

@Composable
private fun CollapseHeader(
    icon: Painter,
    title: String,
    expanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Surface {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(top = 2.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = icon,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            val rotation by animateFloatAsState(if (expanded) 0f else 180f)
            IconButton(onClick = onToggleExpand) {
                Icon(
                    modifier = Modifier.rotate(rotation),
                    painter = painterResource(R.drawable.keyboard_arrow_up_24px),
                    contentDescription = "expand"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    backgroundColor: Color,
    selectMode: Boolean,
    uiState: BookshelfHomeUiState,
    onClickCreate: () -> Unit,
    @Suppress("UNUSED") onClickSearch: () -> Unit,
    onClickEdit: () -> Unit,
    onClickDisableSelectMode: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickPin: () -> Unit,
    onClickRemove: () -> Unit,
    onClickBookmark: () -> Unit,
    onClickShareBookshelf: () -> Unit,
    onClickSaveThisBookshelf: () -> Unit,
    onClickSaveAllBookshelf: () -> Unit,
    onClickImportBookshelf: () -> Unit
) {
    val localDensity = LocalDensity.current
    var mainMenuExpended by remember { mutableStateOf(false) }
    var exportImportMenuExpended by remember { mutableStateOf(false) }
    var mainMenuWidth by remember { mutableStateOf(0.dp) }
    var mainMenuItemHeight by remember { mutableStateOf(0.dp) }
    var exportImportMenuWidth by remember { mutableStateOf(0.dp) }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Box(Modifier.align(Alignment.TopEnd)) {
            DropdownMenu(
                modifier = Modifier
                    .onGloballyPositioned { layoutCoordinates ->
                        with(localDensity) {
                            mainMenuWidth = layoutCoordinates.size.width.toDp()
                            mainMenuItemHeight = layoutCoordinates.size.height.toDp().div(4)
                        }
                    },
                offset = DpOffset(0.dp, (-1).dp),
                expanded = mainMenuExpended,
                onDismissRequest = { mainMenuExpended = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.bookshelf_create_title),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = {
                        mainMenuExpended = false
                        onClickCreate.invoke()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.bookshelf_settings),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = {
                        mainMenuExpended = false
                        onClickEdit.invoke()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.share_bookshelf),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    onClick = onClickShareBookshelf
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.import_and_export),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.arrow_right_24px),
                            contentDescription = null
                        )
                    },
                    onClick = { exportImportMenuExpended = true }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = exportImportMenuWidth + mainMenuWidth + 12.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            DropdownMenu(
                modifier = Modifier
                    .onGloballyPositioned { layoutCoordinates ->
                        with(localDensity) {
                            exportImportMenuWidth = layoutCoordinates.size.width.toDp()
                        }
                    },
                offset = DpOffset(0.dp, mainMenuItemHeight.times(3.5f)),
                expanded = exportImportMenuExpended,
                onDismissRequest = { exportImportMenuExpended = false }
            ) {
                DropdownMenuItem(
                    text = { Text(
                        text = stringResource(R.string.export_to_lnr_file),
                        style = MaterialTheme.typography.bodyLarge
                    ) },
                    onClick = {
                        onClickSaveThisBookshelf()
                        exportImportMenuExpended = false
                        mainMenuExpended = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(
                        text = stringResource(R.string.export_all_to_lnr_file),
                        style = MaterialTheme.typography.bodyLarge
                    ) },
                    onClick = {
                        onClickSaveAllBookshelf()
                        exportImportMenuExpended = false
                        mainMenuExpended = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(
                        text = stringResource(R.string.import_from_file),
                        style = MaterialTheme.typography.bodyLarge
                    ) },
                    onClick = {
                        onClickImportBookshelf()
                        exportImportMenuExpended = false
                        mainMenuExpended = false
                    }
                )
            }
        }
    }

    MediumTopAppBar(
        title = {
            AnimatedText(
                text = if (selectMode) stringResource(R.string.nav_bookshelf_select_mode, uiState.selectedBookIds.size)
                    else stringResource(R.string.nav_bookshelf),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            AnimatedVisibility(visible = selectMode) {
                IconButton(onClickDisableSelectMode) {
                    Icon(
                        painter = painterResource(R.drawable.cancel_24px),
                        contentDescription = "cancel"
                    )
                }
            }
        },
        actions = {
            if (!selectMode) {
                IconButton(onClickCreate) {
                    Icon(
                        painter = painterResource(R.drawable.library_add_24px),
                        contentDescription = "create"
                    )
                }
                IconButton(onClick = { mainMenuExpended = true }) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert_24px), null
                    )
                }
            } else {
                IconButton(onClickSelectAll) {
                    Icon(
                        painter = painterResource(R.drawable.select_all_24px),
                        contentDescription = "select all"
                    )
                }
                IconButton({ onClickPin() }) {
                    Icon(
                        painter = painterResource(R.drawable.keep_24px),
                        contentDescription = "pin"
                    )
                }
                IconButton(onClickRemove) {
                    Icon(
                        painter = painterResource(R.drawable.bookmark_remove_24px),
                        contentDescription = "remove"
                    )
                }
                IconButton(onClickBookmark) {
                    Icon(
                        painter = painterResource(R.drawable.outline_bookmark_24px),
                        contentDescription = "bookmark"
                    )
                }
            }
        },
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        scrollBehavior = scrollBehavior,
        colors = if (uiState.selectMode) TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            scrolledContainerColor = backgroundColor
        ) else TopAppBarDefaults.topAppBarColors()
    )
}

@Suppress("DuplicatedCode")
fun createBookshelfDataFile(fileName: String, launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Documents")
    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/x-lightnovelreader-data"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
        putExtra(Intent.EXTRA_TITLE, "$fileName.lnr")
    }
    launcher.launch(Intent.createChooser(intent, "选择一位置"))
}

@Composable
fun launcher(block: (Uri) -> Unit): ManagedActivityResultLauncher<Intent, ActivityResult> {
    return rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            activityResult.data?.data?.let { uri ->
                block(uri)
            }
        }
    }
}

@Suppress("DuplicatedCode")
fun selectBookshelfDataFile(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
    val initUri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", "primary:Documents")
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initUri)
    }
    launcher.launch(Intent.createChooser(intent, "选择数据文件"))
}