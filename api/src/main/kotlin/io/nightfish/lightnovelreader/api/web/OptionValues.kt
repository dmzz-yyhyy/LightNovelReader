package io.nightfish.lightnovelreader.api.web

/**
 * 带有默认值的选项键
 * 读写[OptionValues]时应使用注册选项时的同一个键实例
 *
 * @param T 选项值的类型
 * @property id 选项在同一组内的唯一标识
 * @param defaultValue 选项的初始值
 * @param copyValue 用于保存选项值的复制函数，选项值可变时应返回不可变副本
 *
 * @since Api 4
 */
class OptionKey<T : Any>(
    val id: String,
    defaultValue: T,
    private val copyValue: (T) -> T = { it }
) {
    /**
     * 该选项的默认值
     *
     * @since Api 4
     */
    val defaultValue: T = snapshot(defaultValue)

    internal fun snapshot(value: T): T = copyValue(value)
}

/**
 * 保存搜索设置或过滤器选项值的不可变快照
 *
 * @since Api 4
 */
class OptionValues private constructor(private val values: Map<OptionKey<*>, Any>) {
    /**
     * 获取指定选项的当前值
     *
     * @param T 选项值的类型
     * @param key 选项注册时使用的键
     * @return 当前选项值，未设置时返回[OptionKey.defaultValue]
     *
     * @since Api 4
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: OptionKey<T>): T =
        values[key] as T? ?: key.defaultValue

    /**
     * 更新指定选项的值
     * 原有快照中的选项值保持不变
     *
     * @param T 选项值的类型
     * @param key 选项注册时使用的键
     * @param value 要设置的选项值
     * @return 更新后的选项快照
     *
     * @since Api 4
     */
    fun <T : Any> with(key: OptionKey<T>, value: T): OptionValues {
        val snapshot = key.snapshot(value)
        return when {
            get(key) == snapshot -> this
            key.defaultValue == snapshot -> OptionValues(values - key)
            else -> OptionValues(values + (key to snapshot))
        }
    }

    /**
     * 比较两个选项快照
     *
     * @param other 用于比较的对象
     * @return 两个快照保存的键和值相同时为 `true`
     *
     * @since Api 4
     */
    override fun equals(other: Any?): Boolean = other is OptionValues && values == other.values

    /**
     * 计算选项快照的哈希值
     *
     * @return 根据保存的键和值计算的哈希值
     *
     * @since Api 4
     */
    override fun hashCode(): Int = values.hashCode()

    /**
     * 提供默认的选项快照
     *
     * @since Api 4
     */
    companion object {
        /**
         * 未设置任何选项的快照
         * 读取其中的选项时均返回各自的默认值
         *
         * @since Api 4
         */
        val Empty = OptionValues(emptyMap())
    }
}
