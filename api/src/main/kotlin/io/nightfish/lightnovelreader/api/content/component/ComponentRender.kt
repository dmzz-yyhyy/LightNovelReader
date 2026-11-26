package io.nightfish.lightnovelreader.api.content.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.render.AbstractContentComponentRender

/**
 * 内容组件渲染入口
 * 根据组件数据查找已注册的渲染器并绘制组件
 *
 * @since Api 4
 */
interface ComponentRender {
    /**
     * 使用已注册的渲染器绘制内容组件
     *
     * @param modifier 传入的Modifier修饰符
     * @param orientation 组件渲染方向
     * @param componentData 待渲染的组件数据
     *
     * @since Api 4
     */
    @Composable
    fun Component(
        modifier: Modifier = Modifier,
        orientation: WriteMode,
        componentData: AbstractContentComponentData
    )

    /**
     * 返回此组件注册的原始 renderer 实例，可直接检查其实现的 cap。
     * 后端使用 `ContentAny` 和 cap 的 `measureAny` 传入运行时组件数据。
     *
     * @param componentData 待查找渲染器的组件数据
     *
     * @return 与组件数据匹配的已注册渲染器
     *
     * @throws IllegalStateException 组件尚未注册。
     * @throws IllegalArgumentException 组件数据与注册的数据类型不匹配。
     * @since Api 4
     */
    fun getRender(componentData: AbstractContentComponentData): AbstractContentComponentRender<*, *>
}
