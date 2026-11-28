package io.nightfish.lightnovelreader.api.web.search

import io.nightfish.lightnovelreader.api.web.explore.filter.Filter

/**
 * [SearchProvider]的抽象实现
 * 子类可在初始化阶段调用注册方法添加搜索设置和结果过滤器
 *
 * @since Api 2
 */
abstract class AbstractSearchProvider : SearchProvider {
    private val registeredSettings = mutableListOf<SearchSetting<*>>()
    private val registeredFilters = mutableListOf<Filter<*>>()

    override val settings: List<SearchSetting<*>> get() = registeredSettings.toList()
    override val filters: List<Filter<*>> get() = registeredFilters.toList()

    /**
     * 注册一个搜索设置
     *
     * @param setting 要注册的搜索设置
     * @throws IllegalArgumentException 已有搜索设置使用相同的选项标识
     *
     * @since Api 4
     */
    protected fun registerSearchSetting(setting: SearchSetting<*>) {
        require(registeredSettings.none { it.key.id == setting.key.id })
        registeredSettings.add(setting)
    }

    /**
     * 注册一个搜索结果过滤器
     *
     * @param filter 要注册的结果过滤器
     * @throws IllegalArgumentException 已有结果过滤器使用相同的选项标识
     *
     * @since Api 4
     */
    protected fun registerSearchFilter(filter: Filter<*>) {
        require(registeredFilters.none { it.key.id == filter.key.id })
        registeredFilters.add(filter)
    }
}
