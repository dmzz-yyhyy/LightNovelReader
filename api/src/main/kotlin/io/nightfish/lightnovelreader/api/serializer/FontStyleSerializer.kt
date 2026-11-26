package io.nightfish.lightnovelreader.api.serializer

import androidx.compose.ui.text.font.FontStyle
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 字体样式序列化器
 * 以整数保存样式, 1表示斜体, 其他值还原为正常样式
 *
 * @since Api 4
 */
object FontStyleSerializer : KSerializer<FontStyle> {

    /**
     * 字体样式的序列化结构描述符
     *
     * @since Api 4
     */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "FontStyle",
            PrimitiveKind.INT
        )

    /**
     * 将字体样式写入编码器
     *
     * @param encoder 编码器
     * @param value 待序列化的字体样式
     *
     * @since Api 4
     */
    override fun serialize(
        encoder: Encoder,
        value: FontStyle
    ) {
        encoder.encodeInt(
            if (value == FontStyle.Italic) 1 else 0
        )
    }

    /**
     * 从解码器读取字体样式
     *
     * @param decoder 解码器
     *
     * @return 反序列化得到的字体样式
     *
     * @since Api 4
     */
    override fun deserialize(
        decoder: Decoder
    ): FontStyle {
        return when (decoder.decodeInt()) {
            1 -> FontStyle.Italic
            else -> FontStyle.Normal
        }
    }
}