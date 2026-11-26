package io.nightfish.lightnovelreader.api.serializer

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 文本单位序列化器
 * 保留sp、em与未指定状态
 *
 * @since Api 4
 */
object TextUnitSerializer : KSerializer<TextUnit> {
    /**
     * 文本单位的序列化结构描述符
     *
     * @since Api 4
     */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "TextUnit",
            PrimitiveKind.STRING
        )

    /**
     * 将文本单位写入编码器
     *
     * @param encoder 编码器
     * @param value 待序列化的文本单位
     *
     * @since Api 4
     */
    override fun serialize(
        encoder: Encoder,
        value: TextUnit
    ) {
        val text = when (value.type) {
            TextUnitType.Sp -> "${value.value}sp"
            TextUnitType.Em -> "${value.value}em"
            TextUnitType.Unspecified -> "unspecified"
            else -> "unspecified"
        }

        encoder.encodeString(text)
    }

    /**
     * 从解码器读取文本单位
     *
     * @param decoder 解码器
     *
     * @return 反序列化得到的文本单位
     *
     * @since Api 4
     */
    override fun deserialize(
        decoder: Decoder
    ): TextUnit {
        val value = decoder.decodeString()

        return when {
            value.endsWith("sp") ->
                value.removeSuffix("sp").toFloat().sp

            value.endsWith("em") ->
                value.removeSuffix("em").toFloat().em

            else ->
                TextUnit.Unspecified
        }
    }
}