package io.nightfish.lightnovelreader.api.content.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.render.AbstractContentComponentRender

interface ComponentRender {
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
     * @throws IllegalStateException 组件尚未注册。
     * @throws IllegalArgumentException 组件数据与注册的数据类型不匹配。
     * @since Api 4
     */
    fun getRender(componentData: AbstractContentComponentData): AbstractContentComponentRender<*, *>
}
