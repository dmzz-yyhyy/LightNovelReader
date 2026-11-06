package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyCachedWebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyCoalescingWebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyPriorityWebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyWebBookDataSource
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.web.WebBookDataSource

class MutableWebDataSourceProvider : WebBookDataSourceProvider {
    private var _value: ProxyWebBookDataSource = wrap(EmptyWebDataSource)
    private val _sources = linkedMapOf<Identifier, ProxyWebBookDataSource>()

    override val value: ProxyWebBookDataSource
        get() = _value

    override fun get(id: Identifier): ProxyWebBookDataSource? = _sources[id]

    override fun getAll(): List<ProxyWebBookDataSource> = _sources.values.toList()

    override fun isWebDataSourceFounded(): Boolean = _value.origin !is NotFoundWebDataSource

    /**
     * 同时更新所有已激活的数据源, 并指定 [primaryId] 作为主数据源。
     * 未注册的数据源不会被加入可查询列表。
     */
    fun updateAll(
        webBookDataSources: Map<Identifier, WebBookDataSource>,
        primaryId: Identifier
    ) {
        _sources.clear()
        webBookDataSources.forEach { (id, dataSource) ->
            _sources[id] = wrap(dataSource)
        }
        _value = _sources[primaryId] ?: wrap(NotFoundWebDataSource(primaryId))
    }

    private fun wrap(webBookDataSource: WebBookDataSource) =
        ProxyCachedWebBookDataSource(
            ProxyCoalescingWebBookDataSource(
                ProxyPriorityWebBookDataSource(webBookDataSource)
            )
        )
}
