package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.data.BookData
import indi.dmzz_yyhyy.lightnovelreader.data.book.Book

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderActivity(bookUID: String?) {
    var book: Book? = null
    if (bookUID == null) {
        error("uid was null")
    } else {
        book = BookData.getBook(bookUID.toString())
    }
    if (book == null) {
        error("book not found")
    }
    val chapter by remember { mutableStateOf(book.bookContent.chaptersList[0]) }
    val chapterTitle by remember { mutableStateOf(book.bookContent.chaptersList[0].chapterTitle) }
    val chapterContent by remember { mutableStateOf(book.bookContent.chaptersList[0].contentText) }
    var isVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = { isVisible = !isVisible }
            )
        },
        topBar = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(400)),
                exit = fadeOut(animationSpec = tween(400))
            ) {
                TopAppBar(title = { Text(chapterTitle) })
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(initialOffsetY = { 2* it / 3 }, animationSpec = tween(400)),
                exit = slideOutVertically(targetOffsetY = { 2* it / 3}, animationSpec = tween(400))
            ) {
                BottomAppBar {}
            }
        }
    ) {
        TextFragment(chapterContent)
    }
}

@Preview(showBackground = true)
@Composable
fun ReaderActivityPreview() {
    ReaderActivity("2c1d3800e0b785bf7905d0f758d8396d")
}