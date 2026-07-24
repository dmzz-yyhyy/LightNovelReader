package io.nightfish.lightnovelreader.api.content.component

import android.content.Context
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.serialization.json.JsonElement
import org.dom4j.Element

/**
 * 内容组件数据抽象基类
 * 所有自定义内容组件数据需继承此类
 *
 * @since Api 2
 */
abstract class AbstractContentComponentData {
    /**
     * 数据组件的唯一标识
     *
     * @since Api 4
     */
    abstract val id: Identifier

    /**
     * 将数据序列化为JSON元素
     *
     * @return 序列化后的[JsonElement]
     *
     * @since Api 2
     */
    abstract fun toJsonElement(): JsonElement

    /**
     * 将数据转化为HTML元素, 用于EPUB导出
     *
     * @param context Android上下文
     *
     * @return 转化后的HTML[Element][org.dom4j.Element]
     *
     * @since Api 2
     */
    abstract fun toHtmlElement(context: Context): Element
}
