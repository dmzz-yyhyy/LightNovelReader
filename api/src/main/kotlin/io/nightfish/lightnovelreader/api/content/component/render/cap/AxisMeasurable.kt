package io.nightfish.lightnovelreader.api.content.component.render.cap

import android.content.Context
import androidx.compose.ui.unit.Density
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.render.AbstractContentComponentRender

/**
 * 按固定交叉轴尺寸测绘组件的能力
 * 横排时给定宽度并返回高度, 竖排时给定高度并返回宽度
 *
 * @param Self 渲染器自身类型
 * @param Data 渲染器处理的组件数据类型
 *
 * @since Api 4
 */
interface AxisMeasurable<Self, Data>: RenderCap<Self, Data>
        where Self : AbstractContentComponentRender<Self, Data>,
              Self : AxisMeasurable<Self, Data>,
              Data : AbstractContentComponentData
{
    /**
     * 对组件进行测量
     * 交叉轴尺寸固定, 返回书写方向上的尺寸
     *
     * @param orientation 组件渲染方向
     * @param context 软件上下文
     * @param density 屏幕密度与字体缩放信息
     * @param crossSize 交叉轴可用大小, 横排为宽度, 竖排为高度, 单位为px
     * @param data 组件数据
     *
     * @return 测绘结果, 横排为高度, 竖排为宽度, 单位为px
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
     * @param orientation 组件渲染方向
     * @param context 软件上下文
     * @param density 屏幕密度与字体缩放信息
     * @param crossSize 交叉轴可用大小, 横排为宽度, 竖排为高度, 单位为px
     * @param data 与当前渲染器注册类型匹配的组件数据
     *
     * @return 书写方向上的组件大小, 单位为px
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
