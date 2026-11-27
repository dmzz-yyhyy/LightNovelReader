package io.nightfish.lightnovelreader.api.web.explore.filter

import androidx.compose.runtime.Composable
import io.nightfish.lightnovelreader.api.web.OptionKey
import io.nightfish.lightnovelreader.api.web.OptionValues
import io.nightfish.lightnovelreader.api.web.PageBook

/**
 * 探索页和搜索结果页的过滤器状态
 * 分别保存数据源过滤器和本地过滤器使用的选项快照
 *
 * @property dataSource 数据源过滤器的选项快照
 * @property local 本地过滤器的选项快照
 *
 * @since Api 4
 */
data class FilterValues(
    val dataSource: OptionValues = OptionValues.Empty,
    val local: OptionValues = OptionValues.Empty
)

/**
 * 探索页和搜索结果页过滤器的抽象基类
 * 子类定义过滤条件的生效方式，插件提供选项键和对应的渲染器
 *
 * @param T 过滤器持有值的类型
 * @property key 过滤器使用的选项键
 * @param renderer 展示当前值的 Compose 渲染器，提交修改时调用其回调
 *
 * @since Api 2
 */
sealed class Filter<T : Any>(
    val key: OptionKey<T>,
    private val renderer: @Composable (value: T, onChange: (T) -> Unit) -> Unit
) {
    /**
     * 使用当前状态渲染过滤器
     * 提交修改时，会将更新合并到最新的过滤器状态
     *
     * @param values 用于读取最新过滤器状态的函数
     * @param onChange 接收更新后的过滤器状态的回调
     *
     * @since Api 4
     */
    @Composable
    fun Content(values: () -> FilterValues, onChange: (FilterValues) -> Unit) {
        renderer(read(values())) { value -> onChange(write(values(), value)) }
    }

    /**
     * 根据指定的本地过滤器状态生成书目过滤函数
     *
     * @param values 本次过滤使用的本地选项快照
     * @return 对应书目应当保留时返回 `true` 的函数
     *
     * @since Api 4
     */
    abstract fun predicate(values: OptionValues): (PageBook) -> Boolean

    internal abstract fun read(values: FilterValues): T
    internal abstract fun write(values: FilterValues, value: T): FilterValues
}

/**
 * 由数据源处理过滤条件的过滤器
 * 状态变化后，宿主使用新的过滤条件从首页重新加载
 *
 * @param T 过滤器持有值的类型
 * @param key 过滤器使用的选项键
 * @param renderer 过滤器的 Compose 渲染器
 *
 * @since Api 4
 */
class DataSourceFilter<T : Any>(
    key: OptionKey<T>,
    renderer: @Composable (T, (T) -> Unit) -> Unit
) : Filter<T>(key, renderer) {
    override fun read(values: FilterValues): T = values.dataSource[key]
    override fun write(values: FilterValues, value: T): FilterValues =
        values.copy(dataSource = values.dataSource.with(key, value))

    /**
     * 获取应用于本地书目的过滤函数
     *
     * @param values 本次过滤使用的本地选项快照
     * @return 始终返回 `true` 的函数，数据源过滤条件在请求书目时生效
     *
     * @since Api 4
     */
    override fun predicate(values: OptionValues): (PageBook) -> Boolean = { true }
}

/**
 * 用于过滤已加载书目的本地过滤器
 * 状态变化后，宿主直接使用[matches]对应的过滤函数处理列表
 *
 * @param T 过滤器持有值的类型
 * @param key 过滤器使用的选项键
 * @param renderer 过滤器的 Compose 渲染器
 * @param matches 根据当前值和已加载书目判定该项是否保留
 *
 * @since Api 4
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
