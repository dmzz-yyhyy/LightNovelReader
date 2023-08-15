package indi.dmzz_yyhyy.lightnovelreader.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NavigateBefore
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import indi.dmzz_yyhyy.lightnovelreader.data.local.RouteConfig
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SearchBar


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, searchViewModel: SearchViewModel) {
    var text by remember { mutableStateOf(searchViewModel.uiState.value.keyword) }
    val searchNavController = rememberNavController()
    Scaffold(
        topBar = {
            SearchBar(
                modifier = Modifier.padding(16.dp),
                value = text,
                label = "enter book name",
                onValueChange = { text = it },
                onDone = { searchViewModel.onClickSearch(searchNavController, text) },
                trailingIcon = {
                    IconButton(onClick = { searchViewModel.onClickSearch(searchNavController, text) }) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "search"
                        )
                    }
                }
            )
        }
    ) {
        Box(Modifier.padding(it)) {
            NavHost(
                navController = searchNavController,
                startDestination = RouteConfig.HOME,
            ) {
                composable(route = RouteConfig.HOME) {
                    Home(searchNavController, searchViewModel)
                }
                composable(route = RouteConfig.SEARCH) {
                    Search(searchNavController, searchViewModel)
                }
            }
            searchViewModel.uiState.value.route?.let { it1 -> searchNavController.navigate(it1) }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(searchNavController: NavController, searchViewModel: SearchViewModel){
    Text("QwQ")
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(searchNavController: NavController, searchViewModel: SearchViewModel) {
    val density = LocalDensity.current.density
    val searchUiState by searchViewModel.uiState.collectAsState()
    if (searchUiState.isLoading){
        Loading()
    }
    else {
        var maxHeight by remember { mutableStateOf(0.dp) }
        var isGetMaxHeight by remember { mutableStateOf( false ) }
        if (maxHeight == 0.dp && !isGetMaxHeight) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(3)
            ) {
                items(searchUiState.bookList) {
                    Card(Modifier
                        .padding(8.dp)
                        .width(100.dp)
                        .onSizeChanged { newSize ->
                            val heightDp = (newSize.height / density).dp
                            if (heightDp > maxHeight) {
                            maxHeight = heightDp
                        }
                        }) {
                        Column(Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = it.coverUrl,
                                contentDescription = "cover",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Text(
                                modifier = Modifier.padding(3.dp, bottom = 0.dp, top = 6.dp),
                                text = it.title,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontFamily = FontFamily.SansSerif
                                )
                            )
                            Box(Modifier.fillMaxWidth().padding(6.dp, end = 2.dp)) {
                                Text(
                                    modifier = Modifier.align(Alignment.TopStart),
                                    text = "writer:" + it.writer,
                                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Box(Modifier.fillMaxWidth().padding(6.dp, top = 0.dp, bottom = 4.dp)) {
                                Text(
                                    modifier = Modifier.align(Alignment.BottomEnd),
                                    text = "tags:" + it.tags.joinToString(" "),
                                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }
            }
            isGetMaxHeight = true
        }
        else {
            Column {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Fixed(3)
                ) {
                    items(searchUiState.bookList) {
                        Card(
                            Modifier
                                .padding(8.dp)
                                .width(100.dp)
                                .height(maxHeight)
                        ) {
                            Column(
                                Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    AsyncImage(
                                        model = it.coverUrl,
                                        contentDescription = "cover",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(190.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Text(
                                        modifier = Modifier.padding(3.dp, bottom = 0.dp, top = 6.dp),
                                        text = it.title,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontFamily = FontFamily.SansSerif
                                        )
                                    )
                                }
                                Column {
                                    Box(Modifier.fillMaxWidth().padding(6.dp, end = 2.dp)) {
                                        Text(
                                            modifier = Modifier.align(Alignment.TopStart),
                                            text = "writer:" + it.writer,
                                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                    Box(Modifier.fillMaxWidth().padding(6.dp, top = 0.dp, bottom = 4.dp)) {
                                        Text(
                                            modifier = Modifier.align(Alignment.BottomEnd),
                                            text = "tags:" + it.tags.joinToString(" "),
                                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item(key=1) {
                        Row (Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            )
                            .clip(CircleShape)) {
                            Box(Modifier.fillMaxHeight().weight(1f)){
                                Text(modifier = Modifier
                                    .align(Alignment.Center),
                                    text = "1",
                                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface))
                            }
                            Box(Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.outline))
                            Box(Modifier.fillMaxHeight().weight(1f).padding(start = 12.dp, end = 12.dp)){
                                Icon(
                                    imageVector = Icons.Outlined.NavigateBefore,
                                    contentDescription = "before",
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(18.dp))
                            }
                            Box(Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.outline))
                            Box(Modifier.fillMaxHeight().weight(1f).padding(start = 12.dp, end = 12.dp)){
                                Text(modifier = Modifier
                                    .align(Alignment.Center),
                                    text = searchUiState.page.toString(),
                                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface))
                            }
                            Box(Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.outline))
                            Box(Modifier.fillMaxHeight().weight(1f).padding(start = 12.dp, end = 12.dp)){
                                Icon(
                                    imageVector = Icons.Outlined.NavigateNext,
                                    contentDescription = "next",
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(18.dp))
                            }
                            Box(Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.outline))
                            Box(Modifier.fillMaxHeight().weight(1f).padding(start = 12.dp, end = 12.dp)){
                                Text(modifier = Modifier
                                    .align(Alignment.Center),
                                    text = searchUiState.totalPage.toString(),
                                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onSurface))
                            }
                    }
                }
                }
            }
        }
    }
}