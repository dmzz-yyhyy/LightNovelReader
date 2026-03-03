package indi.dmzz_yyhyy.lightnovelreader.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalImageHeaderGetter

@Composable
fun Cover(width: Dp, height: Dp, uri: Uri, rounded: Dp = 8.dp) {
    val imagerHeader = LocalImageHeaderGetter.current
    Box(modifier = Modifier
        .size(width, height)
        .graphicsLayer {
            shape = RoundedCornerShape(rounded)
            clip = true
        }
    ) {
        Box(
            Modifier
                .size(width, height)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size((width.times(0.33898306f)))
                    .align(Alignment.Center)
            )
        }
        val imageLoader = LocalContext.current.imageLoader

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .apply {
                    for (entry in imagerHeader()) {
                        addHeader(entry.key, entry.value)
                    }
                }
                .build(),
            contentDescription = "cover",
            contentScale = ContentScale.Crop,
            imageLoader = imageLoader,
            modifier = Modifier.size(width, height)
        )

    }
}
