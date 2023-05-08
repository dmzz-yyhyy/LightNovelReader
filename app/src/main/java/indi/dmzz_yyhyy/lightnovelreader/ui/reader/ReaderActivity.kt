package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.annotation.SuppressLint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import indi.dmzz_yyhyy.lightnovelreader.data.BookData
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookContent
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBooksData

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderActivity(bookUID: String?) {
    var book: Book? = null
    if (bookUID == null) { error("uid was null") }
    else {book = BookData.getBook(bookUID.toString())}
    if (book == null) { error("book not found") }
    val chapter by remember { mutableStateOf(book.bookContent.chaptersList[0]) }
    val chapterTitle by remember { mutableStateOf(book.bookContent.chaptersList[0].chapterTitle) }
    val chapterContent by remember { mutableStateOf(book.bookContent.chaptersList[0].contentText) }
    Scaffold (
        topBar = { TopAppBar(title = { Text(chapterTitle) }) },
        bottomBar = { BottomAppBar {  } }
    ) {
        TextFragment(chapterContent)
    }
}

@Preview(showBackground = true)
@Composable
fun ReaderActivityPreview() {
    ReaderActivity("2c1d3800e0b785bf7905d0f758d8396d")
}