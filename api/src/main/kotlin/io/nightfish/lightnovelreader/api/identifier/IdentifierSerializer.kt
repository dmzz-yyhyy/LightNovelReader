package io.nightfish.lightnovelreader.api.identifier

import com.github.michaelbull.result.getOrElse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class IdentifierSerializer : KSerializer<Identifier> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IdentifierSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Identifier = decoder.decodeString().toId().getOrElse {
        it.ofAppId()
    }

    override fun serialize(encoder: Encoder, value: Identifier) {
        encoder.encodeString(value.toString())
    }
}