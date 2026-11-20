package indi.dmzz_yyhyy.lightnovelreader.data.image

import android.content.Context
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.util.LruCache
import androidx.compose.ui.unit.IntSize
import indi.dmzz_yyhyy.lightnovelreader.utils.network.UserAgentGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.buffer
import okio.source
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/** Reads encoded image headers, without decoding pixels or fetching the rest of a network image. */
internal object ImageDimensions {
    private const val MAX_HEADER_BYTES = 1024 * 1024
    private const val INITIAL_HEADER_BYTES = 4096
    private data class Probe(val size: IntSize?, val limitReached: Boolean = false)
    private data class CacheKey(val uri: Uri, val headers: Map<String, String>)
    private val cache = LruCache<CacheKey, IntSize>(128)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun read(context: Context, uri: Uri, headers: Map<String, String>): IntSize? {
        if (uri.scheme.equals("http", true) || uri.scheme.equals("https", true)) {
            val key = CacheKey(uri, headers.toMap())
            cache.get(key)?.let { return it }
            return try {
                readNetwork(uri, headers)?.also { cache.put(key, it) }
            } catch (_: IllegalArgumentException) {
                null
            }
        }
        return withContext(Dispatchers.IO) {
            try {
                val stream = if (uri.scheme == "file" && uri.path?.startsWith("/android_asset/") == true) {
                    context.assets.open(uri.path!!.removePrefix("/android_asset/"))
                } else {
                    context.contentResolver.openInputStream(uri)
                }
                stream?.use { readBounds(it, MAX_HEADER_BYTES).size }
            } catch (_: IOException) {
                null
            } catch (_: SecurityException) {
                null
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    private suspend fun readNetwork(uri: Uri, headers: Map<String, String>): IntSize? {
        var limit = INITIAL_HEADER_BYTES
        while (true) {
            val probe = readNetworkHeader(uri, headers, limit)
            if (probe.size != null || !probe.limitReached || limit == MAX_HEADER_BYTES) return probe.size
            limit = minOf(limit * 4, MAX_HEADER_BYTES)
        }
    }

    private suspend fun readNetworkHeader(uri: Uri, headers: Map<String, String>, limit: Int): Probe =
        suspendCancellableCoroutine { continuation ->
            val builder = Request.Builder().url(uri.toString())
            headers.forEach { (name, value) -> builder.header(name, value) }
            if (headers.keys.none { it.equals("User-Agent", ignoreCase = true) }) {
                builder.header("User-Agent", UserAgentGenerator.generate())
            }
            val call = client.newCall(
                builder.header("Range", "bytes=0-${limit - 1}").build()
            )
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resume(Probe(null))
                }

                override fun onResponse(call: Call, response: Response) {
                    val size = try {
                        if (response.isSuccessful) readBounds(response.body.byteStream(), limit) else Probe(null)
                    } catch (_: IOException) {
                        Probe(null)
                    } catch (_: IllegalArgumentException) {
                        Probe(null)
                    } finally {
                        // Cancel before closing: closing alone can drain an ignored Range response.
                        call.cancel()
                        response.close()
                    }
                    continuation.resume(size)
                }
            })
        }

    private fun readBounds(input: InputStream, limit: Int): Probe {
        // Also bound local reads and responses from servers that ignore Range. Do not fall back
        // to loading the complete image when a header is unsupported or exceeds this limit.
        val headerStream = HeaderInputStream(input, limit)
        val source = headerStream.source().buffer()
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(source.peek().inputStream(), null, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return Probe(null, limitReached = headerStream.remaining == 0)
        }
        val orientation = if (options.outMimeType == "image/jpeg") {
            try {
                ExifInterface(source.peek().inputStream()).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )
            } catch (_: IOException) {
                ExifInterface.ORIENTATION_NORMAL
            }
        } else ExifInterface.ORIENTATION_NORMAL
        val swapped = orientation in setOf(
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_TRANSVERSE,
            ExifInterface.ORIENTATION_ROTATE_270
        )
        return Probe(if (swapped) IntSize(options.outHeight, options.outWidth)
        else IntSize(options.outWidth, options.outHeight))
    }

    private class HeaderInputStream(input: InputStream, limit: Int) : FilterInputStream(input) {
        var remaining = limit
            private set
        override fun read(): Int {
            if (remaining == 0) return -1
            return super.read().also { if (it != -1) remaining-- }
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            if (length == 0) return 0
            if (remaining == 0) return -1
            return `in`.read(buffer, offset, minOf(length, remaining))
                .also { if (it > 0) remaining -= it }
        }

        override fun skip(count: Long): Long =
            `in`.skip(minOf(count, remaining.toLong())).also { remaining -= it.toInt() }

        override fun available(): Int = minOf(super.available(), remaining)
    }
}
