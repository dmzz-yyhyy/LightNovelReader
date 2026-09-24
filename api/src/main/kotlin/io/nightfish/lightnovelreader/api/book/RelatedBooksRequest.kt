package io.nightfish.lightnovelreader.api.book

/** 关联书籍的查询类型。 */
enum class RelatedBookKind {
    /** 按作者查询。 */
    AUTHOR,
    /** 按标签查询。 */
    TAG
}

/**
 * 由书籍详情发起的关联书籍查询。
 *
 * @property bookId 发起查询的书籍在当前书源中的 ID
 * @property kind 按作者或标签查询的类型
 * @property value 书源提供的原始作者名或标签值，不经过显示文本转换
 */
data class RelatedBooksRequest(
    val bookId: String,
    val kind: RelatedBookKind,
    val value: String,
)
