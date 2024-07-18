package indi.dmzz_yyhyy.lightnovelreader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun Cover(width: Dp, height: Dp, url: String) {
    Box(modifier = Modifier
        .size(width, height)
        .clip(RoundedCornerShape(14.dp))) {
        Box(
            Modifier
            .size(width, height)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size((width.times(0.33898306f))).align(Alignment.Center)
            )
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url
                )
                .crossfade(true)
                .build(),
            contentDescription = "cover",
            modifier = Modifier
                .size(width, height)
        )
    }
}