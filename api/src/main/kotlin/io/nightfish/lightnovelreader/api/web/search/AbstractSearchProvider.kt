package io.nightfish.lightnovelreader.api.web.search

import io.nightfish.lightnovelreader.api.web.explore.filter.Filter

/** 提供设置和结果筛选器的注册方法。 */
abstract class AbstractSearchProvider : SearchProvider {
    private val registeredSettings = mutableListOf<SearchSetting<*>>()
    private val registeredFilters = mutableListOf<Filter<*>>()

    override val settings: List<SearchSetting<*>> get() = registeredSettings.toList()
    override val filters: List<Filter<*>> get() = registeredFilters.toList()

    /**
     * 注册一个搜索设置。请在初始化时完成注册。
     *
     * @param setting 搜索设置
     */
    protected fun registerSearchSetting(setting: SearchSetting<*>) {
        require(registeredSettings.none { it.key.id == setting.key.id })
        registeredSettings.add(setting)
    }

    /**
     * 注册一个结果筛选器。请在初始化时完成注册。
     *
     * @param filter 结果筛选器
     */
    protected fun registerSearchFilter(filter: Filter<*>) {
        require(registeredFilters.none { it.key.id == filter.key.id })
        registeredFilters.add(filter)
    }
}
