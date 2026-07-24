package indi.dmzz_yyhyy.lightnovelreader.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.SubcomposeAsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.transformations
import indi.dmzz_yyhyy.lightnovelreader.data.image.ImageTransPostProcessingViewModel
import indi.dmzz_yyhyy.lightnovelreader.ui.LocalImageHeaderGetter
import io.nightfish.lightnovelreader.api.image.ImagePostProcessingPipeline
import kotlinx.coroutines.Dispatchers

@Composable
fun Cover(width: Dp, height: Dp, uri: Uri, rounded: Dp = 8.dp) {
    val imageHeaderGetter = LocalImageHeaderGetter.current
    val context = LocalContext.current
    val headers = imageHeaderGetter()
    val imageTransPostProcessingViewModel = hiltViewModel<ImageTransPostProcessingViewModel>()
    val request = remember(uri, headers) {
        val transformations = imageTransPostProcessingViewModel
            .imageTransPostProcessingManager
            .getCoil3Transformations(ImagePostProcessingPipeline.bookCover, uri)
        ImageRequest.Builder(context)
            .data(uri)
            .transformations(transformations)
            .crossfade(true)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .interceptorCoroutineContext(Dispatchers.Default)
            .httpHeaders(
                NetworkHeaders.Builder().apply {
                    headers.forEach { (key, value) -> add(key, value) }
                }.build()
            )
            .build()

    }
    Box(
        modifier = Modifier
            .size(width, height)
            .graphicsLayer {
                shape = RoundedCornerShape(rounded)
                clip = true
            }
    ) {
        SubcomposeAsyncImage(
            model = request,
            contentDescription = "cover",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(width, height),
            loading = {
                Box(
                    modifier = Modifier
                        .size(width, height)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(width.times(0.3f))
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .size(width, height)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
            },

        )
    }
}
