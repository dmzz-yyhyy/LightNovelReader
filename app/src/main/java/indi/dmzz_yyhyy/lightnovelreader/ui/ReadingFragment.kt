package indi.dmzz_yyhyy.lightnovelreader.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBooksData

@Preview(showBackground = true)
@Composable
fun ReadingFragment() {
    val padding = 16.dp
    Column(
        Modifier
            .padding(padding)
            .fillMaxWidth()
    ) {
        ReadingBookCard(LocalBooksData.book1)
    }
}
