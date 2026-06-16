package io.nightfish.lightnovelreader.api.content.component

import android.content.Context
import io.nightfish.lightnovelreader.api.identifier.ofAppId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.dom4j.DocumentHelper
import org.dom4j.Element

/**
 * 简单文本组件数据
 * 用于在章节内容中嵌入纯文本段落
 *
 * @param text 文本内容, 支持多行(\n分隔)
 *
 * @since Api 2
 */
@Serializable
data class SimpleTextComponentData(
    val text: String
): AbstractContentComponentData() {
    override val id = Companion.id
    override fun toJsonElement(): JsonElement = Json.encodeToJsonElement(this)

    override fun toHtmlElement(context: Context): Element = DocumentHelper.createElement("div").apply {
        this@SimpleTextComponentData.text
            .replace("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "")
            .split("\n")
            .forEach {
                addText(it)
                addElement("br")
            }
    }

    /**
     * [SimpleTextComponentData]工厂方法和常量集合
     *
     * @since Api 2
     */
    companion object {
        /** 简单文本组件的唯一标识字符串 */
        val id =  "simple_text".ofAppId()
        /** 默认JSON序列化器 */
        val jsonSerializer = object: ComponentDataJsonElementSerializer<SimpleTextComponentData> {
            override fun toJsonElement(data: SimpleTextComponentData): JsonElement = Json.encodeToJsonElement(data)
            override fun fromJsonElement(json: JsonElement): SimpleTextComponentData = Json.decodeFromJsonElement(json)
        }
    }
}