package io.nightfish.lightnovelreader.api.identifier

import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching

/**
 * 解析标识符字符串
 *
 * @return 成功时包含标识符, 失败时包含原始字符串
 *
 * @since Api 4
 */
fun String.toId() = runCatching { Identifier.parse(this) }.mapError { this }
internal fun <T : Identifier> String.ofAppId(factory: (String, String) -> T): T =
    factory(Identifier.HOST_NAMESPACE, this)
