package io.nightfish.lightnovelreader.api.userdata

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 颜色用户数据的序列化器
 * 使用无符号整数字符串保留完整颜色值
 *
 * @since Api 4
 */
object UserDataColorSerializer : KSerializer<Color> {
    /** 序列化描述符 @since Api 4 */
    override val descriptor = PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    /**
     * 序列化用户数据
     *
     * @param encoder 序列化编码器
     * @param value 待序列化的值
     *
     * @since Api 4
     */
    override fun serialize(encoder: Encoder, value: Color) =
        encoder.encodeString(value.value.toString())

    /**
     * 反序列化用户数据
     *
     * @param decoder 序列化解码器
     * @return 反序列化后的值
     *
     * @since Api 4
     */
    override fun deserialize(decoder: Decoder) = Color(decoder.decodeString().toULong())
}

/**
 * URI 用户数据的序列化器
 *
 * @since Api 4
 */
object UserDataUriSerializer : KSerializer<Uri> {
    /** 序列化描述符 @since Api 4 */
    override val descriptor = PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

    /**
     * 序列化用户数据
     *
     * @param encoder 序列化编码器
     * @param value 待序列化的值
     *
     * @since Api 4
     */
    override fun serialize(encoder: Encoder, value: Uri) = encoder.encodeString(value.toString())

    /**
     * 反序列化用户数据
     *
     * @param decoder 序列化解码器
     * @return 反序列化后的值
     *
     * @since Api 4
     */
    override fun deserialize(decoder: Decoder): Uri = decoder.decodeString().toUri()
}
