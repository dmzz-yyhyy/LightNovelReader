package indi.dmzz_yyhyy.lightnovelreader.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import coil3.BitmapImage
import coil3.ImageLoader
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImageUtils {
    suspend fun uriToBitmap(
        imageUri: Uri,
        context: Context,
        header: Map<String, String> = emptyMap()
    ):  Result<Bitmap, Throwable> = withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUri)
                .interceptorCoroutineContext(Dispatchers.IO)
                .httpHeaders(
                    NetworkHeaders.Builder().apply {
                        header.forEach { (key, value) -> add(key, value) }
                    }.build()
                )
                .build()

            val result = loader.execute(request)

            if (result is SuccessResult) {
                val bitmap = (result.image as? BitmapImage)?.bitmap
                if (bitmap != null) {
                    return@withContext Ok(bitmap)
                } else {
                    return@withContext Err(Throwable("Failed to cast image to BitmapImage"))
                }
            } else if (result is ErrorResult) {
                return@withContext Err(result.throwable)
            } else {
                return@withContext Err(Throwable("Unknown result type"))
            }
        } catch (e: Throwable) {
            withContext(Dispatchers.Main) {
                return@withContext Err(Throwable("Failed to cast drawable to BitmapDrawable"))
            }
        }
    }

    suspend fun saveBitmapAsPng(
        context: Context,
        bitmap: Bitmap
    ): Result<String, Throwable> =
        withContext(Dispatchers.IO) {
            try {
                val fileName = "${System.currentTimeMillis()}.png"
                val mimeType = "image/png"

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/LightNovelReader"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext Err(Throwable("failed to get uri"))

                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)

                Ok(fileName)
            } catch (e: Exception) {
                e.printStackTrace()
                Err(e)
            }
        }
}