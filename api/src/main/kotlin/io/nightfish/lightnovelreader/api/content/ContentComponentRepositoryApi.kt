package io.nightfish.lightnovelreader.api.content

import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponent
import io.nightfish.lightnovelreader.api.content.component.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.ComponentDataJsonElementSerializer
import kotlin.reflect.KClass

/**
 * 内容组件注册接口
 * 提供自定义内容组件的注册能力
 *
 * @since Api 2
 */
interface ContentComponentRepositoryApi {
    /**
     * 内容组件注册构建器接口
     * 用于逐步配置一个内容组件的注册信息
     *
     * @since Api 2
     */
    interface RegisterBuilder {
        /**
         * 指定该组件对应的UI组件类
         *
         * @param value 组件类
         *
         * @return 当前构建器实例, 支持链式调用
         *
         * @since Api 2
         */
        fun component(value: KClass<out AbstractContentComponent<out AbstractContentComponentData>>): RegisterBuilder

        /**
         * 指定该组件对应的数据类
         *
         * @param value 数据类
         *
         * @return 当前构建器实例, 支持链式调用
         *
         * @since Api 2
         */
        fun data(value: KClass<out AbstractContentComponentData>): RegisterBuilder

        /**
         * 指定该组件数据的JSON序列化器
         *
         * @param value JSON序列化器实例
         *
         * @return 当前构建器实例, 支持链式调用
         *
         * @since Api 2
         */
        fun serializer(value: ComponentDataJsonElementSerializer<out AbstractContentComponentData>): RegisterBuilder

        /**
         * 完成注册
         *
         * @since Api 2
         */
        fun register()
    }

    /**
     * 内容组件注册器接口
     * 通过指定组件id开始构建注册
     *
     * @since Api 2
     */
    interface Registrar {
        /**
         * 指定组件id, 并返回构建器
         *
         * @param id 组件的唯一标识字符串
         *
         * @return 注册构建器
         *
         * @since Api 4
         */
        fun id(id: Identifier): RegisterBuilder
    }

    /**
     * 内容组件注册器实例
     *
     * @since Api 2
     */
    val registrar: Registrar
}