package io.nightfish.lightnovelreader.api.book

enum class RelatedBookKind { AUTHOR, TAG }

data class RelatedBooksRequest(
    val bookId: String,
    val kind: RelatedBookKind,
    val value: String,
)
