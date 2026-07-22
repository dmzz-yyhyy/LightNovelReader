package io.nightfish.lightnovelreader.api.content.component

/**
 * 可分割内容组件抽象基类
 * 用于需要根据屏幕高度和宽度分割显示的组件
 *
 * @param T 分割后的子组件类型
 * @param Data 此组件对应的数据类型
 * @param data 组件的数据对象
 *
 * @since Api 2
 */
abstract class AbstractDivisibleContentComponent<T: AbstractContentComponent<Data>, Data: AbstractContentComponentData>(
    data: Data
): AbstractContentComponent<Data>(data) {
    /**
     * 将组件按给定尺寸分割为子组件列表
     *
     * @param height 可用区域的高度(像素)
     * @param width 可用区域的宽度(像素)
     *
     * @return 分割后的子组件列表
     *
     * @since Api 2
     */
    abstract suspend fun split(height: Int, width: Int): List<T>
}