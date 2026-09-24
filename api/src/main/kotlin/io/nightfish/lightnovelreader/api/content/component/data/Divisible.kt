package io.nightfish.lightnovelreader.api.content.component.data

import android.content.Context
import androidx.compose.ui.text.TextStyle
import io.nightfish.lightnovelreader.api.ui.ReaderStyle

/**
 * 可分割内容组件
 * 用于需要根据屏幕高度和宽度分割显示的组件
 *
 * @property index 数据组件被分割后的顺序序号
 * @property split 数据组件是否为被分割后的组件
 *
 * @since Api 4
 */
interface Divisible<Self>
        where Self : AbstractContentComponentData,
              Self : Divisible<Self>
{
    val index: Int
    val split: Boolean

    /**
     * 将组件按给定尺寸分割为组件数据列表
     *
     * @param height 可用区域的高度(像素)
     * @param width 可用区域的宽度(像素)
     * @param readerStyle 阅读器样式设置
     * @param baseStyle 阅读器使用的基底样式
     *
     * @return 分割后的子组件数据列表
     *
     * @since Api 4
     */
    suspend fun split(
        height: Int,
        width: Int,
        context: Context,
        readerStyle: ReaderStyle,
        baseStyle: TextStyle
    ): List<Self>
}