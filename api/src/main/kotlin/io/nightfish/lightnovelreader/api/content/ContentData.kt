package io.nightfish.lightnovelreader.api.content

import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponentRender

/**
 * 章节内容数据, 封装组件列表
 *
 * @param components 内容组件列表
 *
 * @since Api 2
 */
data class ContentData(
    val components: List<AbstractContentComponentRender<*>>
) {
    /**
     * [ContentData]工厂方法集合
     *
     * @since Api 2
     */
    companion object {
        /**
         * 返回一个空的内容数据
         *
         * @since Api 2
         */
        fun empty() = ContentData(emptyList())
    }
}