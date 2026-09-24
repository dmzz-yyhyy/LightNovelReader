package io.nightfish.potatoepub.builder

import org.dom4j.Document

class Chapter(
    val title: String,
    val chapterContent: Document?,
    val chapters: List<Chapter>?
) {
    val id: String =
        "chapter_" + (if (chapters != null) (chapters.first().id + title.hashCode()).hashCode() else (chapterContent.hashCode() + title.hashCode())).hashCode()

    constructor(title: String, chapterContent: Document) : this(
        title,
        chapterContent,
        chapters = null
    )

    constructor(title: String, chapters: List<Chapter>) : this(title, null, chapters)
}