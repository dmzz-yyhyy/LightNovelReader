package io.nightfish.lightnovelreader.api.content.component

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import io.nightfish.lightnovelreader.api.identifier.ofAppId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.dom4j.DocumentHelper
import org.dom4j.Element

/**
 * 图片组件数据
 * 用于在章节内容中嵌入一张图片
 *
 * @param uri 图片的[Uri]
 *
 * @since Api 2
 */
@Serializable
data class ImageComponentData(
    @Serializable(with = UriSerializer::class)
    val uri: Uri
): AbstractContentComponentData() {
    /**
     * [Uri]的Kotlinx序列化器
     * 将[Uri]序列化为字符串，并将字符串反序列化回[Uri]
     *
     * @since Api 2
     */
    class UriSerializer : KSerializer<Uri> {
        /**
         * 序列化描述符
         *
         * @since Api 2
         */
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Uri", PrimitiveKind.STRING)

        /**
         * 将[Uri]序列化为字符串
         *
         * @param encoder Kotlinx序列化编码器
         * @param value 要序列化的[Uri]
         *
         * @since Api 2
         */
        override fun serialize(encoder: Encoder, value: Uri) {
            encoder.encodeString(value.toString())
        }

        /**
         * 将字符串反序列化为[Uri]
         *
         * @param decoder Kotlinx序列化解码器
         * @return 解析后的[Uri]
         *
         * @since Api 2
         */
        override fun deserialize(decoder: Decoder): Uri {
            val uriString = decoder.decodeString()
            return uriString.toUri()
        }
    }

    override val id = Companion.id
    override fun toJsonElement(): JsonElement = Json.encodeToJsonElement(this)

    override fun toHtmlElement(context: Context): Element = DocumentHelper.createElement("div").apply {
        addElement("img").apply {
            addAttribute("src", uri.toString())
        }
    }

    /**
     * [ImageComponentData]工厂方法和常量集合
     *
     * @since Api 2
     */
    companion object {
        /** 图片组件的唯一标识字符串 */
        val id = "image".ofAppId()
        /** 默认JSON序列化器 */
        val jsonSerializer = object: ComponentDataJsonElementSerializer<ImageComponentData> {
            override fun toJsonElement(data: ImageComponentData): JsonElement = Json.encodeToJsonElement(data)
            override fun fromJsonElement(json: JsonElement): ImageComponentData = Json.decodeFromJsonElement(json)
        }
    }
}