package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.ui.components.Loading

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChapterScreen(navController: NavController, chapterViewModel: ChapterViewModel) {
    val chapterUiState by chapterViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = { chapterViewModel.onClickBackButton() }){
                    Icon(
                        Icons.Outlined.ArrowBack,
                        contentDescription = "back"
                    )
                }},
                title = { Text(stringResource(id = R.string.chapters)) })
        }
    ) {
        LazyColumn {
            item { Box(Modifier.padding(it)) }
            item {
                Card(
                    Modifier
                        .padding(8.dp)
                ) {
                    Box(
                        Modifier.height(200.dp)
                    ) {
                        Row {
                            AsyncImage(
                                model = chapterUiState.bookCoverUrl,
                                contentDescription = "cover",
                                modifier = Modifier
                                    .size(height = 200.dp, width = 137.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(
                                Modifier
                                    .height(136.dp)
                                    .padding(8.dp, top = 16.dp, end = 24.dp),
                                verticalArrangement = Arrangement.Top
                            ) {
                                Text(
                                    text = chapterUiState.bookName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    maxLines = 3
                                )
                                Text(
                                    modifier = Modifier.padding(start = 3.dp),
                                    text = stringResource(id = R.string.summary),
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
                        Text(modifier = Modifier
                            .padding(start = 145.dp, bottom = 36.dp)
                            .align(alignment = Alignment.BottomStart),
                            style = MaterialTheme.typography.bodySmall,
                            text = "上次阅读:")
                        Button(
                            onClick = { },
                            modifier = Modifier
                                .padding(8.dp)
                                .align(alignment = Alignment.BottomEnd)
                        ) {
                            Text(text = stringResource(id = R.string.read))
                        }
                    }
                }



                Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.TopCenter) {
                    Column {
                        Divider()
                        println(chapterUiState.isLoading)
                        if (chapterUiState.isLoading){
                            Loading()
                        }
                        else {
                            Text(
                                text = stringResource(id = R.string.contents),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                            )
                            Box(Modifier.padding(8.dp)) {
                                Column {
                                    for (volume in chapterUiState.volumeList) {
                                        Text(
                                            modifier = Modifier.padding(start = 2.dp),
                                            text = volume.volumeName,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Box(Modifier.padding(8.dp)) {
                                            Column {
                                                for (chapter in volume.chapters) {

                                                    Text(
                                                        modifier = Modifier.padding(start = 2.dp).clickable(
                                                            onClick = {
                                                                chapterViewModel.onClickChapter(
                                                                    navController,
                                                                    chapter.id
                                                                )
                                                            }
                                                        ),
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
    }
}