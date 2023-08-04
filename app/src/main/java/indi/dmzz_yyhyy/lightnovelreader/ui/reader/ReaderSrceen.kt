package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import indi.dmzz_yyhyy.lightnovelreader.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(navController: NavController, readerViewModel: ReaderViewModel) {
    val readerUiState by readerViewModel.uiState.collectAsState()
    val density = LocalDensity.current.density
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    Scaffold(
        topBar = { TopAppBar(title = { Text(readerUiState.title) }) },
        bottomBar = {
            if(readerUiState.isBottomBarOpen){
                BottomAppBar{
                    IconButton(modifier = Modifier.size(48.dp), onClick = {}){
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_more_vert_24),
                            contentDescription = "more"
                        )
                    }
                    IconButton(modifier = Modifier.size(48.dp), onClick = {}){
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_menu_24),
                            contentDescription = "menu"
                        )
                    }
                    IconButton(modifier = Modifier.size(48.dp), onClick = {}){
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_navigate_before_24),
                            contentDescription = "before"
                        )
                    }
                    IconButton(modifier = Modifier.size(48.dp), onClick = {}){
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_navigate_next_24),
                            contentDescription = "next"
                        )
                    }
                    IconButton(modifier = Modifier.size(48.dp), onClick = {}){
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_bookmark_border_24),
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
            val lazyListState = rememberLazyListState()
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
            LaunchedEffect(lazyListState) {
                println("First visible item index: ${lazyListState.firstVisibleItemIndex}")
                println("First visible item scroll offset: ${lazyListState.firstVisibleItemScrollOffset}")
                println("Total items count: ${lazyListState.layoutInfo.totalItemsCount}")
            }
            LaunchedEffect(remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset } }){
                readerViewModel.onClickMiddle(readerUiState.isBottomBarOpen)
            }
        }
    }
}
