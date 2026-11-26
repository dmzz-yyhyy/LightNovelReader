package io.nightfish.lightnovelreader.api.serializer

import androidx.compose.ui.text.style.TextAlign
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 文本对齐方式序列化器
 * 以整数保存[TextAlign]的底层编码
 *
 * @since Api 4
 */
object TextAlignSerializer : KSerializer<TextAlign> {

    /**
     * 文本对齐方式的序列化结构描述符
     *
     * @since Api 4
     */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "TextAlign",
            PrimitiveKind.INT
        )

    /**
     * 将文本对齐方式写入编码器
     *
     * @param encoder 编码器
     * @param value 待序列化的文本对齐方式
     *
     * @since Api 4
     */
    override fun serialize(
        encoder: Encoder,
        value: TextAlign
    ) {
        encoder.encodeInt(value.value)
    }

    /**
     * 从解码器读取文本对齐方式
     *
     * @param decoder 解码器
     *
     * @return 反序列化得到的文本对齐方式
     *
     * @since Api 4
     */
    override fun deserialize(
        decoder: Decoder
    ): TextAlign {
        return TextAlign.valueOf(decoder.decodeInt())
    }
}
