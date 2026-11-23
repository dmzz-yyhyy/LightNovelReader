package io.nightfish.lightnovelreader.api.content.component.render.cap

import android.content.Context
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import io.nightfish.lightnovelreader.api.content.component.WriteMode
import io.nightfish.lightnovelreader.api.content.component.data.AbstractContentComponentData
import io.nightfish.lightnovelreader.api.content.component.render.AbstractContentComponentRender

/**
 * 根据可用空间测绘组件完整尺寸的能力
 * 组件返回自身的宽高, 排版后端据此决定是否切分或缩放
 *
 * @param Self 渲染器自身类型
 * @param Data 渲染器处理的组件数据类型
 *
 * @since Api 4
 */
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
     * @param availableSpace 可用空间的宽高, 单位为px
     * @param context 软件上下文
     * @param density 屏幕密度与字体缩放信息
     * @param data 组件数据
     *
     * @return 测绘结果, 组件的完整宽高, 单位为px
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
     * @param orientation 组件渲染方向
     * @param availableSpace 可用空间的宽高, 单位为px
     * @param context 软件上下文
     * @param density 屏幕密度与字体缩放信息
     * @param data 与当前渲染器注册类型匹配的组件数据
     *
     * @return 组件的完整宽高, 单位为px
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
