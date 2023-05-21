package indi.dmzz_yyhyy.lightnovelreader.ui.reader.fragment

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import indi.dmzz_yyhyy.lightnovelreader.data.BookData
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChapterFragment(navController: NavController, bookUID: String?) {
    val book: Book?
    if (bookUID == null) {
        error("uid was null")
    } else {
        book = BookData.getBook(bookUID.toString())
    }
    if (book == null) {
        error("book not found")
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("DetailPage") })
        }
    ) {
        Column {
            Box(modifier = Modifier.padding(24.dp, top = 76.dp)) {
                Row {
                    AsyncImage(
                        model = book.coverURL,
                        contentDescription = "cover",
                        modifier = Modifier
                            .size(height = 160.dp, width = 110.dp)
                    )
                    Column(
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = book.bookName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 3
                        )
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Button(
                    onClick = { navController.navigate("reader/0") },
                    modifier = Modifier.align(alignment = Alignment.BottomEnd).padding(end = 24.dp)
                ) {
                    Text(text = "阅读")
                }
            }
            Box(modifier = Modifier.padding(top = 30.dp, bottom = 30.dp)) {
                Divider()
                Text(
                    text = "目录",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Center).padding(top = 10.dp)
                )

            }
                LazyColumn {
                    for (chapter in book.bookContent.chaptersList) {
                        item {


                        }
                    }
                }

        }
    }
}