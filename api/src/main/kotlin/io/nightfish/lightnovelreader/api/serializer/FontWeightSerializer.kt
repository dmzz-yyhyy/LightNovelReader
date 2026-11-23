package io.nightfish.lightnovelreader.api.serializer

import androidx.compose.ui.text.font.FontWeight
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 字体粗细序列化器
 * 以整数保存字重数值
 *
 * @since Api 4
 */
object FontWeightSerializer : KSerializer<FontWeight> {

    /**
     * 字体粗细的序列化结构描述符
     *
     * @since Api 4
     */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "FontWeight",
            PrimitiveKind.INT
        )

    /**
     * 将字体粗细写入编码器
     *
     * @param encoder 编码器
     * @param value 待序列化的字体粗细
     *
     * @since Api 4
     */
    override fun serialize(
        encoder: Encoder,
        value: FontWeight
    ) {
        encoder.encodeInt(value.weight)
    }

    /**
     * 从解码器读取字体粗细
     *
     * @param decoder 解码器
     *
     * @return 反序列化得到的字体粗细
     *
     * @since Api 4
     */
    override fun deserialize(
        decoder: Decoder
    ): FontWeight {
        return FontWeight(
            decoder.decodeInt()
        )
    }
}