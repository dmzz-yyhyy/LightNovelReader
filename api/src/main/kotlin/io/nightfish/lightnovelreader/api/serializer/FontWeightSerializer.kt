package io.nightfish.lightnovelreader.api.serializer

import androidx.compose.ui.text.font.FontWeight
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FontWeightSerializer : KSerializer<FontWeight> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "FontWeight",
            PrimitiveKind.INT
        )

    override fun serialize(
        encoder: Encoder,
        value: FontWeight
    ) {
        encoder.encodeInt(value.weight)
    }

    override fun deserialize(
        decoder: Decoder
    ): FontWeight {
        return FontWeight(
            decoder.decodeInt()
        )
    }
}