package indi.dmzz_yyhyy.lightnovelreader.ui.home.iteams

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import indi.dmzz_yyhyy.lightnovelreader.data.BookData
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBooksData

@Composable
fun ReadingBookCard(bookData: BookData) {
    val padding = 8.dp
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(4.dp, top = padding, bottom = padding * 2),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = bookData.coverURL,
                contentDescription = "cover",
                modifier = Modifier
                    .size(height = 128.dp, width = 88.dp)
            )
            Column(
                modifier = Modifier.padding(start = 8.dp, end = padding * 2),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = bookData.bookName,
                    style = MaterialTheme.typography.titleLarge
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

@Preview
@Composable
fun PreviewReadingBookCard() {
    ReadingBookCard(LocalBooksData.booksDataList[0])
}