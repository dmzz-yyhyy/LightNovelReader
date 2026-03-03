package io.nightfish.lightnovelreader.api.text

import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.ComponentDataJsonElementSerializer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlin.reflect.KClass

class ComponentProcessor(
    val serializerMap: Map<String, ComponentDataJsonElementSerializer<out AbstractContentComponentData>>,
    val dataKClassMap: Map<String, KClass<out AbstractContentComponentData>>,
    var content: JsonObject
) {
    inline fun <reified T: AbstractContentComponentData> process(crossinline block: (T) -> T) {
        content = buildJsonObject {
            putJsonArray("components") {
                content["components"]
                    ?.jsonArray
                    ?.mapNotNull { it.jsonObject }
                    ?.forEach {
                        val id = it["id"]?.jsonPrimitive?.content
                            ?: return@forEach
                        val data = it["data"]?.jsonObject
                            ?: return@forEach
                        if (dataKClassMap[id] != T::class) {
                            addJsonObject {
                                put("id", id)
                                put("data", data)
                            }
                            return@forEach
                        }
                        val serializer = serializerMap[id]
                            ?: return@forEach
                        addJsonObject {
                            put("id", id)
                            put(
                                "data",
                                block(serializer.fromJsonElement(data) as T).toJsonElement()
                            )
                        }
                    }
            }
        }
    }

    fun get() = content
}