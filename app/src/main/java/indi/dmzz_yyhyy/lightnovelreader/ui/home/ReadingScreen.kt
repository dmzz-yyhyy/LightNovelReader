package indi.dmzz_yyhyy.lightnovelreader.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.room.entity.BookMetadata

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun ReadingScreen(
    readingViewModel: ReadingViewModel,
) {
    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = { TopAppBar(title = { Text(stringResource(id = R.string.nav_reading)) }) }
    ) {
        Box(Modifier.padding(it)) {
            val readingUiState by readingViewModel.uiState.collectAsState()
            val padding = 16.dp
            LazyColumn(
                Modifier
                    .padding(start = padding, end = padding)
                    .fillMaxWidth()
            ) {
                items(readingUiState.readingBookDataList) {
                    ReadingBookCard(readingViewModel, it)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingBookCard(readingViewModel: ReadingViewModel, book: BookMetadata) {
    val context = LocalContext.current
    OutlinedCard(
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        onClick = { readingViewModel.onCardClick(book.id, context) }

    ) {
        Row(Modifier.fillMaxWidth()) {
            AsyncImage(
                model = book.coverUrl,
                contentDescription = stringResource(id = R.string.desc_cover),
                modifier = Modifier
                    .size(height = 128.dp, width = 88.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Box(Modifier.height(128.dp)) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = book.name,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1
                    )
                    Text(
                        text = stringResource(id = R.string.current_reading),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomEnd),
                    text = stringResource(id = R.string.last_reading_time),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}



