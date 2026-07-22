package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.data.web.proxy.ProxyWebBookDataSource

/**
 * provider内的WebBookDataSource会变化, 请确保使用的是最新的值
 *
 * @since Api 4
 */
interface WebBookDataSourceProvider {
    fun isWebDataSourceFounded(): Boolean
    val value: ProxyWebBookDataSource
}

