package io.nightfish.lightnovelreader.api.identifier

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 指定标识符类型的序列化器
 * 保持 type:namespace:id 字符串格式, 反序列化时检查目标类型
 *
 * @param T 标识符类型
 * @param type 预期的标识符类
 *
 * @since Api 4
 */
class TypedIdentifierSerializer<T : Identifier>(private val type: Class<T>) : KSerializer<T> {
    private val serializer = IdentifierSerializer()

    /** 序列化描述符 @since Api 4 */
    override val descriptor: SerialDescriptor = serializer.descriptor

    /**
     * 序列化标识符
     *
     * @param encoder 序列化编码器
     * @param value 标识符
     *
     * @since Api 4
     */
    override fun serialize(encoder: Encoder, value: T) = serializer.serialize(encoder, value)

    /**
     * 解析并校验标识符类型
     *
     * @param decoder 序列化解码器
     * @return 指定类型的标识符
     *
     * @since Api 4
     */
    override fun deserialize(decoder: Decoder): T {
        val value = serializer.deserialize(decoder)
        if (!type.isInstance(value)) throw SerializationException("Unexpected identifier type: $value")
        return requireNotNull(type.cast(value))
    }
}
