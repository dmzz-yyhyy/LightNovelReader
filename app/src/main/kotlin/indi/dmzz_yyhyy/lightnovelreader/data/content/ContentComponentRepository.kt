package indi.dmzz_yyhyy.lightnovelreader.data.content

import indi.dmzz_yyhyy.lightnovelreader.data.content.component.ErrorContentComponent
import indi.dmzz_yyhyy.lightnovelreader.data.content.component.ImageComponent
import indi.dmzz_yyhyy.lightnovelreader.data.content.component.SimpleTextComponent
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.PluginInjectorProvider
import io.nightfish.lightnovelreader.api.content.ContentComponentRepositoryApi
import io.nightfish.lightnovelreader.api.content.ContentData
import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponent
import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.ComponentDataJsonElementSerializer
import io.nightfish.lightnovelreader.api.content.component.ImageComponentData
import io.nightfish.lightnovelreader.api.content.component.SimpleTextComponentData
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class ContentComponentRepository @Inject constructor(
    val pluginInjectorProvider: PluginInjectorProvider
): ContentComponentRepositoryApi {
    private val serializeMutableMap = mutableMapOf<String, ComponentDataJsonElementSerializer<out AbstractContentComponentData>>()
    val serializeMap get() =  serializeMutableMap.toMap()
    private val kClassMutableMap = mutableMapOf<String, KClass<out AbstractContentComponent<out AbstractContentComponentData>>>()
    private val dataKClassMutableMap = mutableMapOf<String, KClass<out AbstractContentComponentData>>()
    val dataKClassMap get() = dataKClassMutableMap.toMap()

    fun getContentDataFromJson(jsonObject: JsonObject): ContentData = ContentData(
        jsonObject["components"]
            ?.jsonArray
            ?.mapNotNull { it.jsonObject }
            ?.map {
                val id = it["id"]?.jsonPrimitive?.content
                    ?: return@map ErrorContentComponent.of("component id not found")
                val data = it["data"]?.jsonObject
                    ?: return@map ErrorContentComponent.of("component data not found\nid=$id")
                val kClass = kClassMutableMap[id]
                    ?: return@map ErrorContentComponent.of("component class not found\nid=$id")
                val dataKClass = dataKClassMutableMap[id]
                    ?: return@map ErrorContentComponent.of("component data class not found\nid=$id")
                val serializer = serializeMutableMap[id]
                    ?: return@map ErrorContentComponent.of("component data serializer not found\nid=$id")
                val component =
                    pluginInjectorProvider.value!!.provide<AbstractContentComponent<out AbstractContentComponentData>>(
                        kClass.java,
                        pluginInjectorProvider.value!!.injectMap.toMutableMap().apply {
                            put(dataKClass.java, serializer.fromJsonElement(data) as Any)
                        }
                    ) ?: return@map ErrorContentComponent.of("failed to init component")
                return@map component
            } ?: listOf(ErrorContentComponent.of("error to load components from json"))
    )

    interface Registrar: ContentComponentRepositoryApi.Registrar {
        override fun id(id: String): RegisterBuilder
    }

    override val registrar = object: Registrar {
        override fun id(id: String) = RegisterBuilder(serializeMutableMap, kClassMutableMap, dataKClassMutableMap, id)
    }

    @Suppress("UNCHECKED_CAST")
    class RegisterBuilder(
        private val serializerMap: MutableMap<String, ComponentDataJsonElementSerializer<out AbstractContentComponentData>>,
        private val kClassMap: MutableMap<String, KClass<out AbstractContentComponent<out AbstractContentComponentData>>>,
        private val dataKClassMap: MutableMap<String, KClass<out AbstractContentComponentData>>,
        val id: String
    ): ContentComponentRepositoryApi.RegisterBuilder {
        var componentKClass: KClass<out AbstractContentComponent<out AbstractContentComponentData>>? = null
        var componentDataKClass: KClass<out AbstractContentComponentData>? = null
        var serializer: ComponentDataJsonElementSerializer<out AbstractContentComponentData>? = null

        override fun component(value: KClass<out AbstractContentComponent<out AbstractContentComponentData>>): RegisterBuilder {
            this.componentKClass = value
            return this
        }

        override fun data(value: KClass<out AbstractContentComponentData>): RegisterBuilder {
            this.componentDataKClass = value
            return this
        }

        override fun serializer(value: ComponentDataJsonElementSerializer<out AbstractContentComponentData>): RegisterBuilder {
            this.serializer = value
            return this
        }

        override fun register() {
            if (componentKClass == null || componentDataKClass == null ||serializer == null) throw Error("builder missing parameters")
            kClassMap[id] = componentKClass!!
            dataKClassMap[id] = componentDataKClass!!
            serializerMap[id] = serializer!!
        }
    }
    fun getDataFromJsonObject(content: JsonObject, block: (AbstractContentComponentData) -> Unit) {
        content["components"]
            ?.jsonArray
            ?.mapNotNull { it.jsonObject }
            ?.forEach {
                val id = it["id"]?.jsonPrimitive?.content
                    ?: return@forEach
                val data = it["data"]?.jsonObject
                    ?: return@forEach
                val serializer = serializeMap[id]
                    ?: return@forEach
                block(serializer.fromJsonElement(data))
            }
    }

    init {
        registrar
            .id(SimpleTextComponentData.ID)
            .component(SimpleTextComponent::class)
            .data(SimpleTextComponentData::class)
            .serializer(SimpleTextComponentData.jsonSerializer)
            .register()

        registrar
            .id(ImageComponentData.ID)
            .component(ImageComponent::class)
            .data(ImageComponentData::class)
            .serializer(ImageComponentData.jsonSerializer)
            .register()
    }

}