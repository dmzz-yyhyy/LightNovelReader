package io.nightfish.lightnovelreader.api.serializer

import androidx.compose.ui.text.style.TextAlign
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object TextAlignSerializer : KSerializer<TextAlign> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "TextAlign",
            PrimitiveKind.INT
        )

    override fun serialize(
        encoder: Encoder,
        value: TextAlign
    ) {
        encoder.encodeInt(value.value)
    }

    override fun deserialize(
        decoder: Decoder
    ): TextAlign {
        return TextAlign.valueOf(decoder.decodeInt())
    }
}
