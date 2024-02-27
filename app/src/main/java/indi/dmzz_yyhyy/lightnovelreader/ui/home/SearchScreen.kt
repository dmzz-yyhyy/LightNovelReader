package indi.dmzz_yyhyy.lightnovelreader.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NavigateBefore
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.local.RouteConfig
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SearchBar


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(navController: NavController, searchViewModel: SearchViewModel) {
    var text by remember { mutableStateOf(searchViewModel.uiState.value.keyword) }
    val searchNavController = rememberNavController()
    Scaffold(
        topBar = {
            SearchBar(
                modifier = Modifier.padding(16.dp),
                value = text,
                label = stringResource(id = R.string.title_search),
                onValueChange = { text = it },
                onDone = { searchViewModel.onClickSearch(searchNavController, text) },
                trailingIcon = {
                    val keyboardController = LocalSoftwareKeyboardController.current
                    IconButton(onClick = {
                        keyboardController?.hide()
                        searchViewModel.onClickSearch(searchNavController, text)
                    }) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = stringResource(id = R.string.desc_search)
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
fun Home(searchNavController: NavController, searchViewModel: SearchViewModel) {
    Text("QwQ")
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Search(searchNavController: NavController, searchViewModel: SearchViewModel) {
    val density = LocalDensity.current.density
    val searchUiState by searchViewModel.uiState.collectAsState()
    if (searchUiState.isLoading) {
        Loading()
    } else {
        var maxHeight by remember { mutableStateOf(0.dp) }
        var isGetMaxHeight by remember { mutableStateOf(false) }
        if (maxHeight == 0.dp && !isGetMaxHeight) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Fixed(3)
            ) {
                items(searchUiState.bookList) {
                    Card(
                        Modifier
                        .padding(8.dp)
                        .width(100.dp)
                        .onSizeChanged { newSize ->
                            val heightDp = (newSize.height / density).dp
                            if (heightDp > maxHeight) {
                                maxHeight = heightDp
                            }
                        },
                        colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent)) {
                        Column(Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = it.coverUrl,
                                contentDescription = stringResource(id = R.string.desc_cover),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(155.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Text(
                                modifier = Modifier.padding(bottom = 0.dp, top = 6.dp),
                                text = it.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            )
                            Box(Modifier.fillMaxWidth().padding(end = 2.dp)) {
                                Text(
                                    modifier = Modifier.align(Alignment.TopStart),
                                    text = stringResource(id = R.string.prefix_writer) + " " + it.writer,
                                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Box(Modifier.fillMaxWidth().padding(top = 0.dp, bottom = 4.dp)) {
                                Text(
                                    modifier = Modifier.align(Alignment.BottomEnd),
                                    text = stringResource(id = R.string.prefix_tags) + " " + it.tags.joinToString(" "),
                                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }
            }
            isGetMaxHeight = true
        } else {

            Column {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = TopWithFooter,
                    columns = GridCells.Fixed(3)
                ) {
                    items(searchUiState.bookList) {
                        val context = LocalContext.current
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .padding(8.dp)
                                .width(100.dp)
                                .height(maxHeight),
                            onClick = { searchViewModel.onCardClick(it.id, context) }
                        ) {
                            Column(
                                Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    AsyncImage(
                                        model = it.coverUrl,
                                        contentDescription = stringResource(id = R.string.desc_cover),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(155.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Text(
                                        modifier = Modifier.padding(3.dp, bottom = 0.dp, top = 6.dp),
                                        text = it.name,
                                        maxLines = 3,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )

                                    )
                                }
                                Column {
                                    Box(Modifier.fillMaxWidth().padding(end = 2.dp)) {
                                        Text(
                                            modifier = Modifier.align(Alignment.TopStart),
                                            text = stringResource(id = R.string.prefix_writer) + " " + it.writer,
                                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                    Box(Modifier.fillMaxWidth().padding(top = 0.dp, bottom = 4.dp)) {
                                        Text(
                                            modifier = Modifier.align(Alignment.BottomEnd),
                                            text = stringResource(id = R.string.prefix_tags) + " " + it.tags.joinToString(" "),
                                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {

                            Box(
                                Modifier
                                    .fillMaxWidth()
                            ) {
                                Row(
                                    Modifier
                                        .align(Alignment.Center)
                                ) {
                                    IconButton(onClick = { searchViewModel.onClickFistPageButton() }) {
                                        Text("1", style = MaterialTheme.typography.labelLarge)
                                    }
                                    IconButton(onClick = { searchViewModel.onClickBeforePageButton() }) {
                                        Icon(Icons.Outlined.NavigateBefore, contentDescription = "before page")
                                    }
                                    Box(Modifier.size(48.dp)) {
                                        Text(
                                            searchUiState.page.toString(),
                                            style = MaterialTheme.typography.labelLarge,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                    IconButton(onClick = { searchViewModel.onClickNextPageButton() }) {
                                        Icon(Icons.Outlined.NavigateNext, contentDescription = "next page")
                                    }
                                    IconButton(onClick = { searchViewModel.onClickLastPageButton() }) {
                                        Text(
                                            searchUiState.totalPage.toString(),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
object TopWithFooter : Arrangement.Vertical {
    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        outPositions: IntArray
    ) {
        var y = 0
        sizes.forEachIndexed { index, size ->
            outPositions[index] = y
            y += size
        }
        if (y < totalSize) {
            val lastIndex =
                outPositions.lastIndex
            outPositions[lastIndex] =
                totalSize - sizes.last()
        }
    }
}