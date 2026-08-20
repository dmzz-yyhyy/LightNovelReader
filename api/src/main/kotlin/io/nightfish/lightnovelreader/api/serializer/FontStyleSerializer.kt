package io.nightfish.lightnovelreader.api.serializer

import androidx.compose.ui.text.font.FontStyle
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FontStyleSerializer : KSerializer<FontStyle> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "FontStyle",
            PrimitiveKind.INT
        )

    override fun serialize(
        encoder: Encoder,
        value: FontStyle
    ) {
        encoder.encodeInt(
            if (value == FontStyle.Italic) 1 else 0
        )
    }

    override fun deserialize(
        decoder: Decoder
    ): FontStyle {
        return when (decoder.decodeInt()) {
            1 -> FontStyle.Italic
            else -> FontStyle.Normal
        }
    }
}