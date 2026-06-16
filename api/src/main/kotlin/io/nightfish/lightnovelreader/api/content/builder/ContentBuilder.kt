package io.nightfish.lightnovelreader.api.content.builder

import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponentData
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * 章节内容构建器
 * 用于将多个[AbstractContentComponentData]组件数据组装成[ChapterContent][io.nightfish.lightnovelreader.api.book.ChapterContent]所需的JSON格式
 *
 * @since Api 2
 */
class ContentBuilder {
    /**
     * 待构建的内容组件数据列表
     *
     * @since Api 2
     */
    val components = mutableListOf<AbstractContentComponentData>()

    /**
     * 添加一个内容组件数据
     *
     * @param abstractContentComponentData 需要添加的组件数据
     *
     * @return 当前构建器实例, 支持链式调用
     *
     * @since Api 2
     */
    fun component(abstractContentComponentData: AbstractContentComponentData): ContentBuilder {
        components.add(abstractContentComponentData)
        return this
    }

    /**
     * 构建并返回章节内容的JSON对象
     *
     * @return 包含全部组件的JSON对象
     *
     * @since Api 2
     */
    fun build() =
        buildJsonObject {
            putJsonArray("components") {
                for (component in components) {
                    addJsonObject {
                        put("id", component.id.toString())
                        put("data", component.toJsonElement())
                    }
                }
            }
        }
}