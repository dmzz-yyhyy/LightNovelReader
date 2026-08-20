package io.nightfish.lightnovelreader.api.web.search

import io.nightfish.lightnovelreader.api.util.LocalString

/**
 * [SearchProvider]的抽象实现
 * 提供了以便捷方式注册搜索类型的功能
 *
 * @since Api 2
 */
abstract class AbstractSearchProvider : SearchProvider {
    private val _searchTypes = mutableListOf<SearchType>()
    override val searchTypes: List<SearchType> = _searchTypes

    /**
     * 注册一种搜索类型
     *
     * @param typeId 搜索类型的唯一标识
     * @param name 搜索类型的显示名称
     * @param tip 该搜索类型的输入提示文字
     *
     * @since Api 2
     */
    protected fun registerSearchType(
        typeId: String,
        name: LocalString,
        tip: LocalString
    ) {
        _searchTypes.add(SearchType(typeId, name, tip))
    }
}