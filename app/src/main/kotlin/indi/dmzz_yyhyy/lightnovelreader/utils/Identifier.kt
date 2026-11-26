package indi.dmzz_yyhyy.lightnovelreader.utils

import io.nightfish.lightnovelreader.api.identifier.Identifier

/** 仅用于转换旧版本的书源标识符 */
fun String.convertOldId(): Identifier.DataSource {
    if (startsWith("datasource:")) return Identifier.parseAs<Identifier.DataSource>(this)
    if (this == "-791439186") return Identifier.DataSource("lightnovelreader", "Wenku8")
    val parts = split(':', limit = 2)
    return if (parts.size == 2) Identifier.DataSource(parts[0], parts[1])
    else Identifier.DataSource("lightnovelreader", this)
}

internal fun <T : Identifier> String.ofId(factory: (String, String) -> T): T =
    factory(Identifier.HOST_NAMESPACE, this)
