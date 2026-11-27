package io.nightfish.lightnovelreader.api.web.explore.filter

import androidx.compose.runtime.Composable
import io.nightfish.lightnovelreader.api.web.OptionKey
import io.nightfish.lightnovelreader.api.web.OptionValues
import io.nightfish.lightnovelreader.api.web.PageBook

/**
 * 页面当前的筛选状态。
 *
 * @property dataSource 数据源筛选值
 * @property local 本地筛选值
 */
data class FilterValues(
    val dataSource: OptionValues = OptionValues.Empty,
    val local: OptionValues = OptionValues.Empty
)

/**
 * 由插件提供状态定义和渲染器的筛选器。
 *
 * @param T 筛选值的类型
 * @property key 本筛选器的选项键
 * @param renderer 显示筛选状态，确认修改后调用回调
 */
sealed class Filter<T : Any>(
    val key: OptionKey<T>,
    private val renderer: @Composable (value: T, onChange: (T) -> Unit) -> Unit
) {
    /**
     * 使用当前状态渲染筛选器。
     *
     * @param values 获取最新筛选值。渲染器延后提交时也从这里读取当前值。
     * @param onChange 接收更新后的筛选值
     */
    @Composable
    fun Content(values: () -> FilterValues, onChange: (FilterValues) -> Unit) {
        renderer(read(values())) { value -> onChange(write(values(), value)) }
    }

    /**
     * 为指定状态生成本地筛选函数。
     *
     * @param values 本次本地筛选使用的状态
     */
    abstract fun predicate(values: OptionValues): (PageBook) -> Boolean

    internal abstract fun read(values: FilterValues): T
    internal abstract fun write(values: FilterValues, value: T): FilterValues
}

/**
 * 宿主在状态更新后重新加载的数据源筛选器。
 *
 * @param T 筛选值的类型
 * @param key 本筛选器的选项键
 * @param renderer 筛选器的 Compose 渲染器
 */
class DataSourceFilter<T : Any>(
    key: OptionKey<T>,
    renderer: @Composable (T, (T) -> Unit) -> Unit
) : Filter<T>(key, renderer) {
    override fun read(values: FilterValues): T = values.dataSource[key]
    override fun write(values: FilterValues, value: T): FilterValues =
        values.copy(dataSource = values.dataSource.with(key, value))

    override fun predicate(values: OptionValues): (PageBook) -> Boolean = { true }
}

/**
 * 按状态筛选已加载书目的本地筛选器。
 *
 * @param T 筛选值的类型
 * @param key 本筛选器的选项键
 * @param renderer 筛选器的 Compose 渲染器
 * @param matches 根据当前值和已加载书目判定该项是否保留
 */
class LocalFilter<T : Any>(
    key: OptionKey<T>,
    renderer: @Composable (T, (T) -> Unit) -> Unit,
    private val matches: (T, PageBook) -> Boolean
) : Filter<T>(key, renderer) {
    override fun read(values: FilterValues): T = values.local[key]
    override fun write(values: FilterValues, value: T): FilterValues =
        values.copy(local = values.local.with(key, value))

    override fun predicate(values: OptionValues): (PageBook) -> Boolean {
        val state = values[key]
        return { matches(state, it) }
    }
}
