package indi.dmzz_yyhyy.lightnovelreader.data.web

import io.nightfish.lightnovelreader.api.web.WebBookDataSource

/**
 * provider内的WebBookDataSource会变化, 请确保使用的是最新的值
 */
interface WebBookDataSourceProvider {
    fun isWebDataSourceFounded(): Boolean
    val highPriority: WebBookDataSource
    val default: WebBookDataSource
    val lowPriority: WebBookDataSource
}

