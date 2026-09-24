package io.nightfish.lightnovelreader.api.web.explore.filter

import io.nightfish.lightnovelreader.api.util.LocalString

/**
 * 探索页过滤器的抽象基类
 * 子类封装具体的过滤器类型，并定义标题
 * 当过滤器的[value]发生变化时会自动触发所有已注册的监听器
 *
 * @param T 过滤器持有值的类型
 * @param default 过滤器的默认值
 *
 * @since Api 2
 */
abstract class Filter<T>(default: T) {
    private data class Listener<T>(
        val listener: (T) -> Unit,
        val weight: Int = 0
    )

    /**
     * 过滤器的当前值
     * 修改此属性时将自动触发所有注册的监听器
     *
     * @since Api 2
     */
    var value: T = default
        set(value) {
            field = value
            onChange(value)
        }
    private var listeners = mutableListOf<Listener<T>>()

    /**
     * 获取过滤器的显示标题
     *
     * @return 标题的本地化字符串
     *
     * @since Api 2
     */
    abstract fun getTitle(): LocalString

    private fun onChange(value: T) {
        listeners.forEach {
            it.listener.invoke(value)
        }
    }

    /**
     * 注册一个监听器，当[value]发生变化时调用
     * 监听器按默认权重0插入
     *
     * @param onChange 当值发生变化时被调用的回调
     *
     * @since Api 2
     */
    fun addOnChangeListener(onChange: (T) -> Unit) {
        listeners.add(Listener(onChange))
        listeners = listeners.distinct().sortedBy { it.weight }.reversed().toMutableList()
    }

    /**
     * 注册一个指定权重的监听器，当[value]发生变化时调用
     * 权重越高的监听器越先被调用
     *
     * @param weight 监听器的权重，权重越高越先触发
     * @param onChange 当值发生变化时被调用的回调
     *
     * @since Api 2
     */
    fun addOnChangeListener(weight: Int, onChange: (T) -> Unit) {
        listeners.add(Listener(onChange, weight))
        listeners = listeners.distinct().sortedBy { it.weight }.reversed().toMutableList()
    }
}