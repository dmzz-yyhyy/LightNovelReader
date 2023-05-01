package indi.dmzz_yyhyy.lightnovelreader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import indi.dmzz_yyhyy.lightnovelreader.R
import indi.dmzz_yyhyy.lightnovelreader.data.BookData

@Composable
fun ReadingBookCard(bookData: BookData) {
    val padding = 8.dp
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(4.dp, top = padding, bottom = padding * 2),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = bookData.coverURL,
                contentDescription = "cover",
                modifier = Modifier
                    .size(128.dp)
                    .padding(top = padding, bottom = padding, start = 0.dp, end = 0.dp)
            )
            Column(
                modifier = Modifier.padding(start = 2.dp, end = padding * 2, top = 4.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = bookData.bookName,
                    style = MaterialTheme.typography.titleMedium
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
