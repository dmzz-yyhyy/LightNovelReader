package indi.dmzz_yyhyy.lightnovelreader.ui.home.iteam

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingBookCard(book: Book, onCardClick: (Int, Context) -> Unit) {
    val padding = 8.dp
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(4.dp, top = padding, bottom = padding * 2),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = { onCardClick(book.bookID, context) }


    ) {
        Row(Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = book.coverURL,
                contentDescription = "cover",
                modifier = Modifier
                    .size(height = 128.dp, width = 88.dp)
            )
            Column(
                modifier = Modifier.padding(start = 8.dp, end = padding * 2),
                verticalArrangement = Arrangement.Top
            ) {
                        Text(
                            text = book.bookName,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1
                        )
                Divider(
                    color = Color.Black,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = padding / 2)
                )
                Text(
                    text = "Content",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
