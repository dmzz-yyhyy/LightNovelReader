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

object TextUnitSerializer : KSerializer<TextUnit> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "TextUnit",
            PrimitiveKind.STRING
        )

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