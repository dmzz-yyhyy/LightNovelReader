package indi.dmzz_yyhyy.lightnovelreader.ui.reader

import android.annotation.SuppressLint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderFragment(navController: NavController, bookUID: Int?, chapterIndex: Int?) {
    """var book: Book? = null
    if (bookUID == null) {
        error("uid was null")
    } else {
        book = BookData.getBook(bookUID.toString())
    }
    if (book == null) {
        error("book not found")
    }
    val chapter by remember { mutableStateOf(book.bookContent.chaptersList[chapterIndex!!]) }
    val chapterTitle by remember { mutableStateOf(chapter.chapterTitle) }
    val chapterContent by remember { mutableStateOf(chapter.contentText) }
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
    }"""
}
