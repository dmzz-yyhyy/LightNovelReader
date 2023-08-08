package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.ModalSideSheet

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(navController: NavController, readerViewModel: ReaderViewModel) {
    val readerUiState by readerViewModel.uiState.collectAsState()
    val density = LocalDensity.current.density
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Scaffold(
        topBar = { TopAppBar(
            navigationIcon = {
                IconButton(onClick = { readerViewModel.onClickBackButton(navController) }){
                Icon(
                    Icons.Outlined.ArrowBack,
                    contentDescription = "back"
                )
            }},
            title = { Text(readerUiState.title) }) },
        bottomBar = {
            if(readerUiState.isBottomBarOpen){
                BottomAppBar{
                    IconButton(modifier = Modifier.size(48.dp), onClick = {}){
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = "more"
                        )
                    }
                    IconButton(modifier = Modifier.size(48.dp), onClick = { readerViewModel.onClickMenuButton() }){
                        Icon(
                            Icons.Outlined.Menu,
                            contentDescription = "menu"
                        )
                    }
                    IconButton(modifier = Modifier.size(48.dp), onClick = { readerViewModel.onClickBeforeButton(readerUiState.chapterId) }){
                        Icon(
                            Icons.Outlined.NavigateBefore,
                            contentDescription = "before"
                        )
                    }
                    IconButton(modifier = Modifier.size(48.dp), onClick = { readerViewModel.onClickNextButton(readerUiState.chapterId) }){
                        Icon(
                            Icons.Outlined.NavigateNext,
                            contentDescription = "next"
                        )
                    }
                    IconButton(modifier = Modifier.size(48.dp), onClick = {}){
                        Icon(
                            Icons.Outlined.BookmarkBorder,
                            contentDescription = "bookmark"
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
                val y = (offset.y/density).dp
                if(it.calculateTopPadding() < y && y < screenHeight-80.dp-it.calculateTopPadding()){
                    readerViewModel.onClickMiddle(readerUiState.isBottomBarOpen)
                }
                else{
                    readerViewModel.onClickBelow()
                }
            }
        }) {
            if (readerUiState.isLoading){
                Box(Modifier
                    .fillMaxSize(),
                    contentAlignment = Alignment.Center){
                    Image(
                        modifier = Modifier.size(108.dp, 108.dp),
                        painter = painterResource(id = R.drawable.loading),
                        contentDescription = "Icon Image",
                        contentScale = ContentScale.Crop)
                }
            }
            else {
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
                                .padding(16.dp),
                            text = readerUiState.content,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    if (readerUiState.isSideSheetsOpen){
        Box(Modifier.fillMaxWidth().fillMaxHeight()) {
            ModalSideSheet(modifier = Modifier
                .align(alignment = Alignment.TopEnd),
                closeButton = {
                    IconButton(onClick = { readerViewModel.onCloseChapterSideSheets() }) {
                        Icon(
                            Icons.Outlined.Close,
                            contentDescription = "close"
                        )
                    }
                },
                onClickScrim = { readerViewModel.onCloseChapterSideSheets() },
                title = {
                    Text(
                        text = stringResource(id = R.string.contents),
                        style = MaterialTheme.typography.titleMedium
                    )
                }) {
                LazyColumn {
                    item {
                        Column {
                            for (volume in readerUiState.volumeList) {
                                Text(
                                    modifier = Modifier.padding(start = 2.dp),
                                    text = volume.volumeName,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Box(Modifier.padding(8.dp)) {
                                    Column {
                                        for (chapter in volume.chapters) {

                                            Text(
                                                modifier = Modifier.padding(start = 2.dp),
                                                text = chapter.title,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Divider()
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
