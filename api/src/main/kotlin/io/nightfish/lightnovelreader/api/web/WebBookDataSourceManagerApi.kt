package io.nightfish.lightnovelreader.api.web

import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * 网络书本数据源管理接口
 * 提供注册、注销及获取已激活数据源的能力
 *
 * @since Api 2
 */
interface WebBookDataSourceManagerApi {
    /**
     * 注册一个网络数据源
     *
     * @param webBookDataSource 要注册的数据源实现
     * @param webDataSourceItem 该数据源的元数据描述
     *
     * @since Api 2
     */
    fun registerWebDataSource(
        webBookDataSource: WebBookDataSource,
        webDataSourceItem: WebDataSourceItem
    )

    /**
     * 注销指定id的网络数据源
     *
     * @param webDataSourceId 要注销的数据源id
     *
     * @since Api 4
     */
    fun unregisterWebDataSource(webDataSourceId: Identifier)

    /**
     * 获取当前激活的网络数据源
     *
     * @return 当前已激活的[WebBookDataSource]实例
     *
     * @since Api 2
     */
    fun getWebDataSource(): WebBookDataSource
}