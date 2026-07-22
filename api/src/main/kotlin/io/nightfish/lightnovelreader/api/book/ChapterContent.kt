package io.nightfish.lightnovelreader.api.book

import androidx.compose.runtime.Stable
import kotlinx.serialization.json.JsonObject

/**
 * 章节内容接口
 *
 * @property id 章节id
 * @property title 章节标题
 * @property content 章节内容的JSON对象，内含组件列表
 * @property lastChapter 上一章的章节id，如果没有上一章则为空字符串
 * @property nextChapter 下一章的章节id，如果没有下一章则为空字符串
 *
 * @since Api 4
 */
@Stable
data class ChapterContent(
    val id: String,
    val title: String,
    val content: JsonObject,
    val lastChapter: String?,
    val nextChapter: String?
) {

    /**
     * 判断是否存在上一章
     *
     * @return 是否存在上一章
     *
     * @since Api 2
     */
    fun hasPrevChapter(): Boolean = lastChapter == null

    /**
     * 判断是否存在下一章
     *
     * @return 是否存在下一章
     *
     * @since Api 2
     */
    fun hasNextChapter(): Boolean = nextChapter == null
}