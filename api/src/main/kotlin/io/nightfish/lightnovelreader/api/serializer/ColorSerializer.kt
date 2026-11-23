package io.nightfish.lightnovelreader.api.serializer

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 颜色序列化器
 * 以长整型保存[Color]的底层编码, 保留颜色空间信息
 *
 * @since Api 4
 */
object ColorSerializer : KSerializer<Color> {

    /**
     * 颜色的序列化结构描述符
     *
     * @since Api 4
     */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "Color",
            PrimitiveKind.LONG
        )

    /**
     * 将颜色写入编码器
     *
     * @param encoder 编码器
     * @param value 待序列化的颜色
     *
     * @since Api 4
     */
    override fun serialize(
        encoder: Encoder,
        value: Color
    ) {
        encoder.encodeLong(value.value.toLong())
    }

    /**
     * 从解码器读取颜色
     *
     * @param decoder 解码器
     *
     * @return 反序列化得到的颜色
     *
     * @since Api 4
     */
    override fun deserialize(
        decoder: Decoder
    ): Color {
        return Color(
            decoder.decodeLong().toULong()
        )
    }
}