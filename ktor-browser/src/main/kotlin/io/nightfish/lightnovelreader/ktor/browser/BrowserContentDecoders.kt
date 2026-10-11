package io.nightfish.lightnovelreader.ktor.browser

import com.github.luben.zstd.ZstdInputStream
import io.ktor.client.plugins.compression.ContentEncodingConfig
import io.ktor.util.ContentEncoder
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import io.ktor.utils.io.writeFully
import io.ktor.utils.io.writer
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import org.brotli.dec.BrotliInputStream
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

/** Streaming response decoders for encodings advertised by desktop Chrome. */
internal object BrotliContentDecoder : InputStreamContentDecoder("br", ::BrotliInputStream)

internal object ZstdContentDecoder : InputStreamContentDecoder("zstd", ::ZstdInputStream)

/** Adds response support for the `br` and `zstd` algorithms advertised by Chrome. */
fun ContentEncodingConfig.browserResponseDecoders() {
    customEncoder(BrotliContentDecoder)
    customEncoder(ZstdContentDecoder)
}

internal abstract class InputStreamContentDecoder(
    final override val name: String,
    private val inputStream: (InputStream) -> InputStream,
) : ContentEncoder {
    override fun encode(source: ByteReadChannel, coroutineContext: CoroutineContext): ByteReadChannel = source

    override fun encode(source: ByteWriteChannel, coroutineContext: CoroutineContext): ByteWriteChannel = source

    @OptIn(DelicateCoroutinesApi::class)
    override fun decode(
        source: ByteReadChannel,
        coroutineContext: CoroutineContext,
    ): ByteReadChannel = GlobalScope.writer(coroutineContext + CoroutineName("decode-$name")) {
        inputStream(source.toInputStream(coroutineContext[Job])).use { decoded ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = decoded.read(buffer)
                if (count < 0) break
                channel.writeFully(buffer, 0, count)
            }
        }
    }.channel
}
