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

object TextIndentSerializer : KSerializer<TextIndent> {

    override val descriptor =
        buildClassSerialDescriptor("TextIndent") {
            element("firstLine", TextUnitSerializer.descriptor)
            element("restLine", TextUnitSerializer.descriptor)
        }

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