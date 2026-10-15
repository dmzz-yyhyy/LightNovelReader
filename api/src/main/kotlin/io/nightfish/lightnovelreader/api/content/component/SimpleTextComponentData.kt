package io.nightfish.lightnovelreader.api.content.component

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.dom4j.Namespace
import org.dom4j.QName

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
    override val id: String = ID
    override fun toJsonElement(): JsonElement = Json.encodeToJsonElement(this)

    override fun toHtmlElement(context: Context): Element = DocumentHelper.createElement(QName.get("div", Namespace.get(XHTML_NAMESPACE))).apply {
        this@SimpleTextComponentData.text
            .replace("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]", "")
            .trim()
            .split("\n")
            .let { lines ->
                lines
                    .filterIndexed { index, string ->
                        return@filterIndexed !(string.isBlank() && lines.getOrNull(index - 1)?.isNotBlank() == true)
                    }
            }
            .forEach {
                when {
                    it.isBlank() -> addElement("br", XHTML_NAMESPACE)
                    else -> addElement("p", XHTML_NAMESPACE).apply {
                        text = it.trim()
                    }
                }
            }
    }

    /**
     * [SimpleTextComponentData]工厂方法和常量集合
     *
     * @since Api 2
     */
    companion object {
        /** 简单文本组件的唯一标识字符串 */
        const val ID = "simple_text"
        private const val XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml"
        /** 默认JSON序列化器 */
        val jsonSerializer = object: ComponentDataJsonElementSerializer<SimpleTextComponentData> {
            override fun toJsonElement(data: SimpleTextComponentData): JsonElement = Json.encodeToJsonElement(data)
            override fun fromJsonElement(json: JsonElement): SimpleTextComponentData = Json.decodeFromJsonElement(json)
        }
    }
}