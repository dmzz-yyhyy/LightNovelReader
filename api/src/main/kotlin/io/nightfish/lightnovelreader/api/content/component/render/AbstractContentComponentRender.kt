package io.nightfish.lightnovelreader.api.content.component.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.render.cap.AxisMeasurable
import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * 内容组件抽象基类
 * 所有自定义内容组件需继承此类
 * 此类是单例, 只负责处理组件渲染
 *
 * @param Data 此组件对应的数据类型
 *
 * @since Api 2
 */
abstract class AbstractContentComponentRender<Self, Data>: AxisMeasurable<Self, Data>
        where Data : AbstractContentComponentData,
              Self : AbstractContentComponentRender<Self, Data>
{
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
     * @param orientation 组件渲染方向
     * @param data 组件数据
     *
     * @since Api 2
     */
    @Composable
    abstract fun Content(
        modifier: Modifier,
        orientation: WriteMode,
        data: Data
    )

    /**
     * 供持有星投影 renderer 的渲染后端调用，仍由当前实例执行 [Content]。
     * 调用方须先通过组件注册信息确认 [data] 与此 renderer 匹配。
     * 具体 renderer 的调用方应使用保留 [Data] 类型检查的 [Content]。
     *
     * @since Api 4
     */
    @Composable
    @Suppress("UNCHECKED_CAST")
    fun ContentAny(
        modifier: Modifier,
        orientation: WriteMode,
        data: AbstractContentComponentData
    ) {
        Content(modifier, orientation, data as Data)
    }
}
