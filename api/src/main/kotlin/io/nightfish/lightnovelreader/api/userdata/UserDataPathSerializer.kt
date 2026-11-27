package io.nightfish.lightnovelreader.api.userdata

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 用户数据路径的序列化器
 * 仅保存路径与归属, 数据格式由调用端的值序列化器提供
 *
 * @param T 该路径存储的值类型
 * @param valueSerializer 路径所用的数据序列化器
 *
 * @since Api 4
 */
class UserDataPathSerializer<T>(private val valueSerializer: KSerializer<T>) :
    KSerializer<UserDataPath<T>> {
    @Serializable
    private data class Definition(
        val path: String,
        val namespace: String,
        val sourceScoped: Boolean
    )

    /**
     * 序列化描述符
     *
     * @since Api 4
     */
    override val descriptor: SerialDescriptor = Definition.serializer().descriptor

    /**
     * 序列化用户数据路径
     *
     * @param encoder 序列化编码器
     * @param value 待序列化的路径
     *
     * @since Api 4
     */
    override fun serialize(encoder: Encoder, value: UserDataPath<T>) {
        encoder.encodeSerializableValue(
            Definition.serializer(),
            Definition(value.path, value.namespace, value.sourceScoped)
        )
    }

    /**
     * 反序列化用户数据路径
     *
     * @param decoder 序列化解码器
     * @return 带有原数据格式和归属的用户数据路径
     *
     * @since Api 4
     */
    override fun deserialize(decoder: Decoder): UserDataPath<T> {
        val definition = decoder.decodeSerializableValue(Definition.serializer())
        return UserDataPath(
            definition.path,
            valueSerializer,
            definition.namespace,
            definition.sourceScoped
        )
    }
}
