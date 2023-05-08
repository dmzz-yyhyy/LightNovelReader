package indi.dmzz_yyhyy.lightnovelreader.ui.home.iteam

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBooksData
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalMainNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingBookCard(book: Book) {
    val mainNavController = LocalMainNavController.current
    val padding = 8.dp
    Card(
        onClick = { println(mainNavController);mainNavController.navigate("reader/${book.bookUID}") },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(4.dp, top = padding, bottom = padding * 2),
        elevation = CardDefaults.cardElevation(4.dp)
        // the codec below won't stay there for a long time


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

@Preview
@Composable
fun PreviewReadingBookCard() {
    ReadingBookCard(LocalBooksData.booksDataList[0])
}