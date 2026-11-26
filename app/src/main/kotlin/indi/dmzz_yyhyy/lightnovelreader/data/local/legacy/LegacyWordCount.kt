package indi.dmzz_yyhyy.lightnovelreader.data.local.legacy

import kotlinx.serialization.Serializable

/** 旧归档的字数格式, 不随插件 API 的变化而改变 */
@Serializable
data class LegacyWordCount(val count: Int, val unit: String?, val unitResId: Int?)
