package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyCachedWebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyCoalescingWebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyPriorityWebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyWebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebBookDataSource

class MutableWebDataSourceProvider : WebBookDataSourceProvider {
    private var _value: ProxyWebBookDataSource = ProxyPriorityWebBookDataSource(EmptyWebDataSource)
    override val value: ProxyWebBookDataSource
        get() = _value

    override fun isWebDataSourceFounded(): Boolean = _value.origin !is NotFoundWebDataSource

    fun update(webBookDataSource: WebBookDataSource) {
        _value = ProxyCachedWebBookDataSource(
            ProxyCoalescingWebBookDataSource(
                ProxyPriorityWebBookDataSource(webBookDataSource)
            )
        )
    }

}
