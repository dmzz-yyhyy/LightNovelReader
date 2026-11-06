package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyWebBookDataSource
import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * provider内的WebBookDataSource会变化, 请确保使用的是最新的值
 *
 * 现在支持同时激活多个数据源:
 * [value] 表示当前主数据源(用于探索/搜索等没有书籍上下文的操作),
 * [get] 用于按书籍所属的数据源取回对应的数据源。
 *
 * @since Api 4
 */
interface WebBookDataSourceProvider {
    fun isWebDataSourceFounded(): Boolean
    val value: ProxyWebBookDataSource

    /**
     * 获取指定 id 的数据源, 若该数据源未注册或不可用则返回 null
     */
    fun get(id: Identifier): ProxyWebBookDataSource?

    /**
     * 获取所有已注册且可用的数据源
     */
    fun getAll(): List<ProxyWebBookDataSource>
}
