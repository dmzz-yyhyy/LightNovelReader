package io.nightfish.lightnovelreader.api.book

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable

/**
 * 用于存储书本字数的对象
 * 可以灵活更变其单位
 * 如果unit与unitResId都是null
 * 那么会默认使用 字 作为单位
 *
 * @param count 实际的大小
 * @param unit 单位名称, 其中字符串的{count}部分将被替换为数字, 如果不存在{count}占位符则会直接将单位加到数字后面
 * @param unitResId 单位名称的ResId, 其中字符串的{count}部分将被替换为数字, 如果不存在{count}占位符则会直接将单位加到数字后面
 */
@Serializable
data class WordCount(
    val count: Int,
    val unit: String?,
    @param:StringRes val unitResId: Int?
) {
    constructor(count: Int): this(count, null, null)
    constructor(count: Int, unit: String): this(count, unit, null)
    constructor(count: Int, @StringRes unitResId: Int): this(count, null, unitResId)
}