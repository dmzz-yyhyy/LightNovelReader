package indi.dmzz_yyhyy.lightnovelreader.ui.home.iteam

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingBookCard(book: Book, onCardClick: (Int, Context) -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(top = 8.dp, bottom = 8.dp)
            .fillMaxWidth(),
        onClick = { onCardClick(book.bookID, context) }

    ) {
        Row(Modifier.fillMaxWidth()){
            AsyncImage(
                model = book.coverURL,
                contentDescription = "cover",
                modifier = Modifier
                    .size(height = 128.dp, width = 88.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .height(128.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = book.bookName,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1
                )
                Text(
                    text = stringResource(id = R.string.current_reading),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(modifier = Modifier.align(Alignment.End),
                    text = stringResource(id = R.string.last_reading_time),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.titleSmall
                )
            }
            }
    }
}

