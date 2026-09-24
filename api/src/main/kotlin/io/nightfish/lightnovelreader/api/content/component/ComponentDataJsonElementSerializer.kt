package io.nightfish.lightnovelreader.api.content.component

import kotlinx.serialization.json.JsonElement

/**
 * 内容组件数据的JSON序列化器接口
 * 用于将[io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData]与JSON互转
 *
 * @param Data 序列化器处理的数据类型
 *
 * @since Api 2
 */
interface ComponentDataJsonElementSerializer<Data> {
    /**
     * 将数据对象序列化为JSON元素
     *
     * @param data 需要序列化的数据对象
     *
     * @return 序列化后的[JsonElement]
     *
     * @since Api 2
     */
    fun toJsonElement(data: Data): JsonElement

    /**
     * 将JSON元素反序列化为数据对象
     *
     * @param json 需要反序列化的[JsonElement]
     *
     * @return 反序列化后的数据对象
     *
     * @since Api 2
     */
    fun fromJsonElement(json: JsonElement): Data
}