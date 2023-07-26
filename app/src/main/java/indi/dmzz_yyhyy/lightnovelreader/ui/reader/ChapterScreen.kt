package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChapterScreen(navController: NavController, chapterViewModel: ChapterViewModel) {
    val chapterUiState by chapterViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("章节") })
        }
    ) {
        LazyColumn {
            item {
                Box(modifier = Modifier.padding(24.dp, top = 76.dp)) {
                    Row {
                        AsyncImage(
                            model = chapterUiState.bookCoverUrl,
                            contentDescription = "cover",
                            modifier = Modifier
                                .size(height = 160.dp, width = 110.dp)
                        )
                        Column(
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                text = chapterUiState.bookName,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 3
                            )
                            Text(
                                modifier = Modifier.padding(start = 3.dp),
                                text = "简介",
                                style = MaterialTheme.typography.titleMedium
                            )
                            LazyColumn(Modifier.height(140.dp)) {
                                item {
                                    Text(
                                        text = chapterUiState.bookIntroduction,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                    Button(
                        onClick = { },
                        modifier = Modifier.align(alignment = Alignment.BottomEnd).padding(end = 24.dp)
                    ) {
                        Text(text = "阅读")
                    }
                }
                Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.TopCenter) {
                    Column (){
                    Text(
                        text = "目录",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    Divider()
                        for (volume in chapterUiState.volumeList) {
                                Text(
                                    text = volume.volumeName,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Divider(Modifier.padding(4.dp))
                                Box(Modifier.padding(4.dp)) {
                                    Column {
                                    for (chapter in volume.chapters) {

                                            Text(
                                                text = chapter.title,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Divider(Modifier.padding(2.dp))
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