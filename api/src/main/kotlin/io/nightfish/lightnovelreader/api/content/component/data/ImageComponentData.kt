package io.nightfish.lightnovelreader.api.content.component.data

import android.content.Context
import android.net.Uri
import io.nightfish.lightnovelreader.api.content.component.ComponentDataJsonElementSerializer
import io.nightfish.lightnovelreader.api.identifier.ofAppId
import io.nightfish.lightnovelreader.api.serializer.UriSerializer
import kotlinx.serialization.Serializable
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
) : AbstractContentComponentData() {

    override val id = Companion.id
    override fun toJsonElement(): JsonElement = Json.encodeToJsonElement(this)

    override fun toHtmlElement(context: Context): Element =
        DocumentHelper.createElement("div").apply {
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
        val jsonSerializer = object : ComponentDataJsonElementSerializer<ImageComponentData> {
            override fun toJsonElement(data: ImageComponentData): JsonElement =
                Json.encodeToJsonElement(data)

            override fun fromJsonElement(json: JsonElement): ImageComponentData =
                Json.decodeFromJsonElement(json)
        }
    }
}

