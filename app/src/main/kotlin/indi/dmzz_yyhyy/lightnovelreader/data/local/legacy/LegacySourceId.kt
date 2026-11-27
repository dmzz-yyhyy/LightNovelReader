package indi.dmzz_yyhyy.lightnovelreader.data.local.legacy

import indi.dmzz_yyhyy.lightnovelreader.utils.convertOldId
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** API 2/3 使用整数书源 id, API 4 使用带命名空间的字符串 */
data class LegacySourceId(val id: Identifier.DataSource)
class LegacySourceIdSerializer : KSerializer<LegacySourceId> {
    override val descriptor = PrimitiveSerialDescriptor("LegacySourceId", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): LegacySourceId = try {
        LegacySourceId(decoder.decodeString().convertOldId())
    } catch (_: SerializationException) {
        LegacySourceId(decoder.decodeInt().toString().convertOldId())
    }

    override fun serialize(encoder: Encoder, value: LegacySourceId) =
        encoder.encodeString(value.id.toString())
}
