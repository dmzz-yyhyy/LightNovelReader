package io.nightfish.lightnovelreader.api.identifier

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 标识符的序列化器
 *
 * @since Api 4
 */
class IdentifierSerializer : KSerializer<Identifier> {
    /**
     * 序列化描述符
     *
     * @since Api 4
     */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("IdentifierSerializer", PrimitiveKind.STRING)

    /**
     * 反序列化
     *
     * @since Api 4
     */
    override fun deserialize(decoder: Decoder): Identifier =
        Identifier.parse(decoder.decodeString())

    /**
     * 序列化
     *
     * @since Api 4
     */
    override fun serialize(encoder: Encoder, value: Identifier) {
        encoder.encodeString(value.toString())
    }
}
