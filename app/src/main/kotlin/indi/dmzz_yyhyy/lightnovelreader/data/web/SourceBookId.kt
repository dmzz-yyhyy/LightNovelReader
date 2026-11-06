package indi.dmzz_yyhyy.lightnovelreader.data.web

/**
 * 多数据源下书本 id 的限定方式。
 *
 * 不同数据源可能生成相同的书本 id, 直接以书 id 作为主键会互相覆盖。
 * 因此对来自非主数据源的新数据, 统一使用 `数据源id|原始书id` 作为本地书 id。
 * 旧数据没有前缀, 仍然按原始 id 处理, 由书籍信息里记录的数据源归属兜底。
 */
private const val SEPARATOR = '|'

fun qualifyBookId(sourceId: String, remoteBookId: String): String {
    val prefix = "$sourceId$SEPARATOR"
    return if (remoteBookId.startsWith(prefix)) remoteBookId else prefix + remoteBookId
}

data class ParsedBookId(
    val sourceId: String,
    val remoteBookId: String
)

fun parseQualifiedBookId(bookId: String): ParsedBookId? {
    val index = bookId.indexOf(SEPARATOR)
    if (index <= 0 || index == bookId.lastIndex) return null
    return ParsedBookId(
        sourceId = bookId.substring(0, index),
        remoteBookId = bookId.substring(index + 1)
    )
}
