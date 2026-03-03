package io.nightfish.lightnovelreader.api.content.component

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.dom4j.DocumentHelper
import org.dom4j.Element

@Serializable
data class SimpleTextComponentData(
    val text: String
): AbstractContentComponentData() {
    override val id: String = ID
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

    companion object {
        const val ID = "simple_text"
        val jsonSerializer = object: ComponentDataJsonElementSerializer<SimpleTextComponentData> {
            override fun toJsonElement(data: SimpleTextComponentData): JsonElement = Json.encodeToJsonElement(data)
            override fun fromJsonElement(json: JsonElement): SimpleTextComponentData = Json.decodeFromJsonElement(json)
        }
    }
}