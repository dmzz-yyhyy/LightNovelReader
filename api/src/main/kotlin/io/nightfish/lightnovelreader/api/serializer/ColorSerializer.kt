package io.nightfish.lightnovelreader.api.serializer

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ColorSerializer : KSerializer<Color> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "Color",
            PrimitiveKind.LONG
        )

    override fun serialize(
        encoder: Encoder,
        value: Color
    ) {
        encoder.encodeLong(value.value.toLong())
    }

    override fun deserialize(
        decoder: Decoder
    ): Color {
        return Color(
            decoder.decodeLong().toULong()
        )
    }
}