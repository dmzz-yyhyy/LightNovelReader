package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
                        contentDescription = stringResource(id = R.string.desc_back)
                    )
                }},
                title = { Text(stringResource(id = R.string.chapters)) })
        }
    ) {
        LazyColumn {
            item { Box(Modifier.padding(it)) }
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp)
                ) {
                    Box(Modifier.height(200.dp)) {
                        Row {
                            AsyncImage(
                                model = chapterUiState.bookCoverUrl,
                                contentDescription = stringResource(id = R.string.desc_cover),
                                contentScale = ContentScale.Crop,
                                modifier =
                                Modifier.size(height = 200.dp, width = 137.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Column(
                                Modifier.height(136.dp).padding(12.dp, top = 16.dp),
                                verticalArrangement = Arrangement.Top
                            ) {
                                Text(
                                    text = chapterUiState.bookName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }

                /** SUMMARY */
                Box(Modifier.padding(top = 14.dp)) {
                    Column(Modifier.fillMaxSize()) {
                        var expandSummaryText by remember { mutableStateOf(false) }
                        val text = chapterUiState.bookIntroduction
                        Column(modifier = Modifier.padding(10.dp)) {
                            /**
                             * We're hiding this "Summary" title as it's conspicuous enough and NOT
                             * particularly necessary!
                             *
                             * Text( modifier = Modifier.padding(start = 3.dp), text =
                             * stringResource(id = R.string.summary), style =
                             * MaterialTheme.typography.titleLarge)
                             */
                            Column(
                                modifier =
                                Modifier.animateContentSize(animationSpec = tween(100))
                                    .clickable(
                                        interactionSource =
                                        remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        expandSummaryText = !expandSummaryText
                                    }
                            ) {
                                if (expandSummaryText) {
                                    Text(style = MaterialTheme.typography.titleMedium, text = text)
                                } else {
                                    Text(
                                        style = MaterialTheme.typography.titleMedium,
                                        text = text,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                /** Contents */
                Box(modifier = Modifier.padding(6.dp), contentAlignment = Alignment.TopCenter) {
                    Column {
                        Divider()
                        println(chapterUiState.isLoading)
                        if (chapterUiState.isLoading) {
                            Loading()
                        } else {

                            Box {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = "LastRead"
                                        )
                                        Text(text = stringResource(id = R.string.last_read))
                                    }

                                    Button(
                                        modifier = Modifier.fillMaxWidth(0.75f),
                                        onClick = {
                                            chapterViewModel.onClickReadButton(navController)
                                        }
                                    ) {
                                        Text(text = stringResource(id = R.string.read))
                                    }
                                }
                            }

                            Row(Modifier.padding(start = 6.dp, end = 8.dp)) {
                                Text(
                                    text = stringResource(id = R.string.contents),
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                                var expanded by remember { mutableStateOf(false) }

                                Box(
                                    modifier =
                                    Modifier.fillMaxWidth().wrapContentSize(Alignment.TopEnd)
                                ) {
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(
                                            imageVector = Icons.Default.Sort,
                                            contentDescription = "Sort"
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.sw_chapter_sorting)) },
                                            onClick = { chapterViewModel.toggleChapterSorting() }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(id = R.string.sw_volume_sorting)) },
                                            onClick = { chapterViewModel.toggleVolumeSorting() }
                                        )
                                    }
                                }
                            }

                            Box(Modifier.padding(8.dp)) {
                                Column {
                                    val sortedVolumeList =
                                        if (chapterUiState.isVolumeReversed) {
                                            chapterUiState.volumeList.reversed()
                                        } else {
                                            chapterUiState.volumeList
                                        }
                                    for (volume in sortedVolumeList) {
                                        Text(
                                            modifier = Modifier.padding(start = 2.dp),
                                            text = volume.volumeTitle,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Box(Modifier.padding(8.dp)) {
                                            Column {
                                                val sortedChapters =
                                                    if (chapterUiState.isChapterReversed) {
                                                        volume.chapters.reversed()
                                                    } else {
                                                        volume.chapters
                                                    }
                                                for (chapter in sortedChapters) {
                                                    Text(
                                                        modifier =
                                                        Modifier.padding(start = 2.dp)
                                                            .clickable(
                                                                onClick = {
                                                                    chapterViewModel
                                                                        .onClickChapter(
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
