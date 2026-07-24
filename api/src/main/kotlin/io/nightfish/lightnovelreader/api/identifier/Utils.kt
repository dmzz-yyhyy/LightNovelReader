package io.nightfish.lightnovelreader.api.identifier

import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching

/**
 * 用于将文字格式id转换为id对象的扩展函数
 */
fun String.toId() = runCatching {
    this.split(":", limit = 2).let {
        Identifier(it[0], it[1])
    }
}.mapError { this }

internal fun String.ofAppId() = Identifier("lightnovelreader", this)