package io.nightfish.lightnovelreader.api.web

/**
 * 带默认值的类型化选项键。读取选项时使用注册时的键实例。
 *
 * @param T 选项值的类型
 * @property id 选项在同一组内的唯一标识
 * @property defaultValue 默认状态
 * @param copyValue 将输入转成可安全共享的不可变值
 */
class OptionKey<T : Any>(
    val id: String,
    defaultValue: T,
    private val copyValue: (T) -> T = { it }
) {
    val defaultValue: T = snapshot(defaultValue)

    internal fun snapshot(value: T): T = copyValue(value)
}

/** 一次请求或一次筛选使用的选项快照。 */
class OptionValues private constructor(private val values: Map<OptionKey<*>, Any>) {
    /**
     * 读取选项。未修改时返回默认值。
     *
     * @param key 已注册的类型化键
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: OptionKey<T>): T =
        values[key] as T? ?: key.defaultValue

    /**
     * 返回包含新值的快照。
     *
     * @param key 已注册的类型化键
     * @param value 提交的状态
     */
    fun <T : Any> with(key: OptionKey<T>, value: T): OptionValues {
        val snapshot = key.snapshot(value)
        return when {
            get(key) == snapshot -> this
            key.defaultValue == snapshot -> OptionValues(values - key)
            else -> OptionValues(values + (key to snapshot))
        }
    }

    /** 判断两份快照保存的键和值是否相同。 */
    override fun equals(other: Any?): Boolean = other is OptionValues && values == other.values

    /** 返回与快照内容一致的哈希值。 */
    override fun hashCode(): Int = values.hashCode()

    /** 提供默认选项。 */
    companion object {
        /** 所有选项都使用各自的默认值。 */
        val Empty = OptionValues(emptyMap())
    }
}
