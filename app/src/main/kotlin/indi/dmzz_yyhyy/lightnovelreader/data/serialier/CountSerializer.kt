package indi.dmzz_yyhyy.lightnovelreader.data.serialier

import indi.dmzz_yyhyy.lightnovelreader.data.statistics.Count
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class CountSerializer : KSerializer<Count> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Count", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Count) {
        encoder.encodeString(value.toBase64String())
    }

    override fun deserialize(decoder: Decoder): Count {
        val countString = decoder.decodeString()
        return Count.fromBase64String(countString)
    }
}