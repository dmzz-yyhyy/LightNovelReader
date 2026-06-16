package io.nightfish.lightnovelreader.api.content.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * 内容组件抽象基类
 * 所有自定义内容组件需继承此类
 *
 * @param Data 此组件对应的数据类型
 * @param data 组件的数据对象
 *
 * @since Api 2
 */
abstract class AbstractContentComponent<Data: AbstractContentComponentData>(
    val data: Data
) {
    /**
     * 组件的唯一标识符
     * 应与[AbstractContentComponentData.id]保持一致
     *
     * @since Api 4
     */
    abstract val id: Identifier

    /**
     * 组件的UI渲染函数
     *
     * @param modifier 传入的Modifier修饰符
     *
     * @since Api 2
     */
    @Composable
    abstract fun Content(modifier: Modifier)
}
