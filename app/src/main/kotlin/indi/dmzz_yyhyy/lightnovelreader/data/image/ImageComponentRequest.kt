package indi.dmzz_yyhyy.lightnovelreader.data.image

import android.content.Context
import android.net.Uri
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.transformations
import indi.dmzz_yyhyy.lightnovelreader.utils.network.UserAgentGenerator
import io.nightfish.lightnovelreader.api.image.ImagePostProcessingPipeline
import io.nightfish.lightnovelreader.api.image.ImageTransPostProcessingManagerApi
import kotlinx.coroutines.Dispatchers

internal fun imageComponentRequestBuilder(
    context: Context,
    uri: Uri,
    header: Map<String, String>,
    manager: ImageTransPostProcessingManagerApi
): ImageRequest.Builder = ImageRequest.Builder(context)
    .data(uri)
    .transformations(
        manager.getImageTransformations(ImagePostProcessingPipeline.imageComponent)
            .orEmpty()
            .map { (id, transformation) -> Coil3Transformation(id, uri, transformation) }
    )
    .crossfade(true)
    .memoryCachePolicy(CachePolicy.ENABLED)
    .networkCachePolicy(CachePolicy.ENABLED)
    .interceptorCoroutineContext(Dispatchers.Default)
    .httpHeaders(
        NetworkHeaders.Builder().apply {
            header.forEach { (key, value) -> add(key, value) }
            if (header.keys.none { it.equals("User-Agent", ignoreCase = true) }) {
                add("User-Agent", UserAgentGenerator.generate())
            }
        }.build()
    )
