package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ModalSideSheet

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(navController: NavController, readerViewModel: ReaderViewModel) {
    val readerUiState by readerViewModel.uiState.collectAsState()
    val density = LocalDensity.current.density
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val interactionSource = remember { MutableInteractionSource() }

    Scaffold(
        topBar = {
            AnimatedVisibility (visible = readerUiState.isAppBarVisible) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { readerViewModel.onClickBackButton(navController) }){
                            Icon(
                                Icons.Outlined.ArrowBack,
                                contentDescription = stringResource(id = R.string.desc_back)
                            )
                        }},
                    title = { Text(readerUiState.title, maxLines = 2) })
            }},
        bottomBar = {
            AnimatedVisibility(
                visible = readerUiState.isBottomBarOpen,
            ) {
                BottomAppBar {
                    IconButton(modifier = Modifier.size(48.dp), onClick = {}) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = stringResource(id = R.string.desc_more)
                        )
                    }
                    IconButton(modifier = Modifier.size(48.dp), onClick = { readerViewModel.onClickMenuButton() }) {
                        Icon(
                            Icons.Outlined.Menu,
                            contentDescription = stringResource(id = R.string.desc_menu)
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = { readerViewModel.onClickBeforeButton(readerUiState.chapterId) }) {
                        Icon(
                            Icons.Outlined.NavigateBefore,
                            contentDescription = stringResource(id = R.string.desc_previous)
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = { readerViewModel.onClickNextButton(readerUiState.chapterId) }) {
                        Icon(
                            Icons.Outlined.NavigateNext,
                            contentDescription = stringResource(id = R.string.desc_next)
                        )
                    }
                    IconButton(modifier = Modifier.size(48.dp), onClick = {}) {
                        Icon(
                            Icons.Outlined.BookmarkBorder,
                            contentDescription = stringResource(id = R.string.desc_bookmark)
                        )
                    }
                }
            }
        }
    ) {
        Box(Modifier
            .padding(top = it.calculateTopPadding())
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val y = (offset.y / density).dp
                    if (it.calculateTopPadding() < y && y < screenHeight - 80.dp - it.calculateTopPadding()) {
                        readerViewModel.onClickMiddle(readerUiState.isBottomBarOpen)
                    } else {
                        readerViewModel.onClickBelow()
                    }
                }
            }) {
            if (readerUiState.isLoading) {
                Loading()
            } else {
                val lazyListState = rememberLazyListState()
                var isScrolling by remember { mutableStateOf(false) }
                LaunchedEffect(lazyListState.isScrollInProgress) {
                    isScrolling = lazyListState.isScrollInProgress
                    readerViewModel.onClickMiddle(readerUiState.isBottomBarOpen)
                }
                LazyColumn(state = lazyListState) {
                    item {
                        Text(
                            modifier = Modifier
                                .padding(16.dp, top=0.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = interactionSource,
                                    onClick = {
                                    readerViewModel.onClickText(readerUiState.isAppBarVisible)
                                }),

                            text = readerUiState.content,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
    // 章节选择侧边栏
    if (readerUiState.isSideSheetsOpen) {
        Box(Modifier.fillMaxWidth().fillMaxHeight()) {
            ModalSideSheet(modifier = Modifier.padding(top = 36.dp)
                .align(alignment = Alignment.TopEnd),
                closeButton = {
                    IconButton(onClick = { readerViewModel.onCloseChapterSideSheets() }) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = stringResource(id = R.string.desc_close)
                        )
                    }
                },
                onClickScrim = { readerViewModel.onCloseChapterSideSheets() },
                title = {
                    Text(
                        text = stringResource(id = R.string.contents),
                        style = MaterialTheme.typography.titleLarge
                    )
                }) {
                LazyColumn {
                    item {
                        Column {
                            for (volume in readerUiState.volumeList) {
                                Text(
                                    modifier = Modifier.padding(start = 2.dp),
                                    text = volume.volumeTitle,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Box(
                                    Modifier
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        for (chapter in volume.chapters) {

                                            Text(
                                                modifier = Modifier
                                                    .padding(start = 2.dp)
                                                    .clickable { readerViewModel.onClickChangeChapter(chapter.id) },
                                                text = chapter.title,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            HorizontalDivider()
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
}
