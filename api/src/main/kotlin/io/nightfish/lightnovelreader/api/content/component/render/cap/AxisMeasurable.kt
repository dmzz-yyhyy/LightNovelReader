package io.nightfish.lightnovelreader.api.content.component.render.cap

import android.content.Context
import androidx.compose.ui.unit.Density
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.render.AbstractContentComponentRender

interface AxisMeasurable<Self, Data>: RenderCap<Self, Data>
        where Self : AbstractContentComponentRender<Self, Data>,
              Self : AxisMeasurable<Self, Data>,
              Data : AbstractContentComponentData
{
    /**
     * 对组件进行测量
     * 对于主轴方向的大小固定
     * 要求组件给出排版方向的大小
     *
     * @param orientation 组件渲染方向
     * @param context 软件上下文
     * @param density 屏幕分辨率
     * @param crossSize 主轴可用大小, 根据组件渲染方向决定, 单位为xp
     * @param data 组件数据
     *
     * @return 测绘结果, 控件大小, 单位为xp
     *
     * @since Api 4
     */
    suspend fun measure(
        orientation: WriteMode,
        context: Context,
        density: Density,
        crossSize: Int,
        data: Data
    ): Int

    /**
     * 供渲染后端在 `is AxisMeasurable<*, *>` 判断后调用。
     * [data] 必须匹配该 renderer 注册的数据类型，具体类型的调用方使用 [measure]。
     *
     * @since Api 4
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun measureAny(
        orientation: WriteMode,
        context: Context,
        density: Density,
        crossSize: Int,
        data: AbstractContentComponentData
    ): Int = measure(orientation, context, density, crossSize, data as Data)
}
