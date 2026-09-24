package io.nightfish.lightnovelreader.api.text

import io.nightfish.lightnovelreader.api.content.component.ComponentDataJsonElementSerializer
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlin.reflect.KClass

/**
 * 章节内容组件处理器
 * 用于对章节内容JSON中的特定类型组件批量应用变换操作
 *
 * @param serializerMap 组件id到其JSON序列化器的映射
 * @param dataKClassMap 组件id到其数据类KClass的映射
 * @param content 当前章节内容的JSON对象
 *
 * @since Api 2
 */
class ComponentProcessor(
    val serializerMap: Map<String, ComponentDataJsonElementSerializer<out AbstractContentComponentData>>,
    val dataKClassMap: Map<String, KClass<out AbstractContentComponentData>>,
    var content: JsonObject
) {
    /**
     * 对指定类型的所有组件数据应用变换
     * 不匹配类型的组件将原样保留
     *
     * @param Input 需要处理的组件数据类型
     * @param Output 处理后的组件数据类型
     * @param block 接收原组件数据并返回变换后数据的函数
     *
     * @since Api 2
     */
    inline fun <reified Input, Output : AbstractContentComponentData> process(crossinline block: (Input) -> Output) {
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
                        val serializer = serializerMap[id]
                            ?: return@forEach
                        when (val component = serializer.fromJsonElement(data)) {
                            is Input -> {
                                addJsonObject {
                                    put("id", id)
                                    put(
                                        "data",
                                        block(component).toJsonElement()
                                    )
                                }
                            }

                            else -> {
                                addJsonObject {
                                    put("id", id)
                                    put("data", data)
                                }
                            }
                        }
                    }
            }
        }
    }

    /**
     * 获取处理后的章节内容JSON对象
     *
     * @return 处理后的[JsonObject]
     *
     * @since Api 2
     */
    fun get() = content
}