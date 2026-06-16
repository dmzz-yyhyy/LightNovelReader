package indi.dmzz_yyhyy.lightnovelreader.data.serializer

import com.github.michaelbull.result.onOk
import indi.dmzz_yyhyy.lightnovelreader.utils.convertOldId
import indi.dmzz_yyhyy.lightnovelreader.utils.ofId
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.identifier.toId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class LocalDataIdentifierSerializer : KSerializer<Identifier> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDataIdentifierSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Identifier {
        return try {
            val id = decoder.decodeString()
            id.toId()
                .onOk {
                    return it
                }
            return id.ofId()
        } catch (_: Throwable) {
            try {
                decoder.decodeInt().toString().convertOldId()
            } catch (intError: Throwable) {
                throw SerializationException("Expected String or Int", intError)
            }
        }
    }

    override fun serialize(encoder: Encoder, value: Identifier) {
        encoder.encodeString(value.toString())
    }
}