package indi.dmzz_yyhyy.lightnovelreader.ui.home.fragment

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBooksData
import indi.dmzz_yyhyy.lightnovelreader.ui.home.iteam.ReadingBookCard

@Preview(showBackground = true)
@Composable
fun ReadingFragment() {
    val padding = 16.dp
    Column(
        Modifier
            .padding(padding)
            .fillMaxWidth()
    ) {
        for (bookData in LocalBooksData.booksDataList) {
            ReadingBookCard(bookData)
        }
    }
}


