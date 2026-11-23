package io.nightfish.lightnovelreader.api.serializer

import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

/**
 * 文本缩进序列化器
 * 分别通过[TextUnitSerializer]保存首行与其余行的缩进
 *
 * @since Api 4
 */
object TextIndentSerializer : KSerializer<TextIndent> {

    /**
     * 文本缩进的序列化结构描述符
     *
     * @since Api 4
     */
    override val descriptor =
        buildClassSerialDescriptor("TextIndent") {
            element("firstLine", TextUnitSerializer.descriptor)
            element("restLine", TextUnitSerializer.descriptor)
        }

    /**
     * 将文本缩进写入编码器
     *
     * @param encoder 编码器
     * @param value 待序列化的文本缩进
     *
     * @since Api 4
     */
    override fun serialize(
        encoder: Encoder,
        value: TextIndent
    ) {
        encoder.encodeStructure(descriptor) {

            encodeSerializableElement(
                descriptor,
                0,
                TextUnitSerializer,
                value.firstLine
            )

            encodeSerializableElement(
                descriptor,
                1,
                TextUnitSerializer,
                value.restLine
            )
        }
    }

    /**
     * 从解码器读取文本缩进
     *
     * @param decoder 解码器
     *
     * @return 反序列化得到的文本缩进
     *
     * @since Api 4
     */
    override fun deserialize(
        decoder: Decoder
    ): TextIndent {

        var firstLine = TextUnit.Unspecified
        var restLine = TextUnit.Unspecified

        decoder.decodeStructure(descriptor) {

            while (true) {
                when (decodeElementIndex(descriptor)) {
                    0 -> firstLine =
                        decodeSerializableElement(
                            descriptor,
                            0,
                            TextUnitSerializer
                        )

                    1 -> restLine =
                        decodeSerializableElement(
                            descriptor,
                            1,
                            TextUnitSerializer
                        )

                    CompositeDecoder.DECODE_DONE -> break
                }
            }
        }

        return TextIndent(
            firstLine,
            restLine
        )
    }
}