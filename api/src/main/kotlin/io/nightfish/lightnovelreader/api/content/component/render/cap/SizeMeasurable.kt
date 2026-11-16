package io.nightfish.lightnovelreader.api.content.component.render.cap

import android.content.Context
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.render.AbstractContentComponentRender

interface SizeMeasurable<Self, Data>: RenderCap<Self, Data>
        where Self : AbstractContentComponentRender<Self, Data>,
              Self : SizeMeasurable<Self, Data>,
              Data : AbstractContentComponentData
{
    /**
     * 对组件进行测量
     * 要求组件给出固定的大小
     * 会给出可用空间大小
     *
     * @param orientation 组件渲染方向
     * @param availableSpace 可用空间, 单位为xp
     * @param context 软件上下文
     * @param density 屏幕分辨率
     * @param data 组件数据
     *
     * @return 测绘结果, 控件大小, 单位为xp
     *
     * @since Api 4
     */
    suspend fun measure(
        orientation: WriteMode,
        availableSpace: IntSize,
        context: Context,
        density: Density,
        data: Data
    ): IntSize

    /**
     * 供渲染后端在 `is SizeMeasurable<*, *>` 判断后调用。
     * [data] 必须匹配该 renderer 注册的数据类型，具体类型的调用方使用 [measure]。
     *
     * @since Api 4
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun measureAny(
        orientation: WriteMode,
        availableSpace: IntSize,
        context: Context,
        density: Density,
        data: AbstractContentComponentData
    ): IntSize = measure(orientation, availableSpace, context, density, data as Data)
}
