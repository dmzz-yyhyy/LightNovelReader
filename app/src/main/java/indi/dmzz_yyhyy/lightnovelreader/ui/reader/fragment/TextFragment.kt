package indi.dmzz_yyhyy.lightnovelreader.ui.reader.fragment

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun TextFragment(contentText: String) {
    LazyColumn {
        item {
            Text(
                modifier = Modifier.padding(16.dp),
                text = contentText,
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}
