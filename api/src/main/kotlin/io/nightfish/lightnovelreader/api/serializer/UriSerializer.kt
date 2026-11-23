package io.nightfish.lightnovelreader.api.serializer

import android.net.Uri
import androidx.core.net.toUri
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Uri序列化器
 * 以字符串保存Uri
 *
 * @since Api 4
 */
object UriSerializer : KSerializer<Uri> {
    /**
     * Uri的序列化结构描述符
     *
     * @since Api 4
     */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

    /**
     * 将Uri写入编码器
     *
     * @param encoder 编码器
     * @param value 待序列化的Uri
     *
     * @since Api 4
     */
    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.toString())
    }

    /**
     * 从解码器读取Uri
     *
     * @param decoder 解码器
     *
     * @return 反序列化得到的Uri
     *
     * @since Api 4
     */
    override fun deserialize(decoder: Decoder): Uri {
        val uriString = decoder.decodeString()
        return uriString.toUri()
    }
}