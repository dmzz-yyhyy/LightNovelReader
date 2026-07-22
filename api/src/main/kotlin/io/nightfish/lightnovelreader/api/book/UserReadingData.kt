package io.nightfish.lightnovelreader.api.book

import androidx.compose.runtime.Stable
import java.time.LocalDateTime

/**
 * 用户书本阅读数据接口
 *
 * @property id 书本id
 * @property lastReadTime 最后阅读时间
 * @property totalReadTime 总阅读时长(单位: 秒), 0表示尚未阅读
 * @property readingProgress 书本整体阅读进度(0.0~1.0)
 * @property lastReadChapterId 最后阅读的章节id
 * @property lastReadChapterTitle 最后阅读的章节标题
 * @property currentChapterReadingProgressMap 各章节的当前阅读进度Map, 以章节id为key
 * @property maxChapterReadingProgressMap 各章节的历史最高阅读进度Map, 以章节id为key
 *
 * @since Api 4
 */
@Stable
data class UserReadingData (
    val id: String,
    val lastReadTime: LocalDateTime?,
    val totalReadTime: Int,
    val readingProgress: Float,
    val lastReadChapterId: String?,
    val lastReadChapterTitle: String?,
    val currentChapterReadingProgressMap: Map<String, Float>,
    val maxChapterReadingProgressMap: Map<String, Float>
) {
    companion object {
        fun new(id: String) = UserReadingData(
            id = id,
            lastReadTime = null,
            totalReadTime = 0,
            readingProgress = 0f,
            lastReadChapterId = null,
            lastReadChapterTitle = null,
            currentChapterReadingProgressMap = mapOf(),
            maxChapterReadingProgressMap = mapOf()
        )
    }

    fun copyWithUpdatedChapterReadingProgress(chapterId: String, progress: Float): UserReadingData {
        val maxProgress = progress.coerceAtLeast(this.maxChapterReadingProgressMap[chapterId] ?: 0f)
        val currentChapterReadingProgressMap = this.currentChapterReadingProgressMap.toMutableMap()
        val maxChapterReadingProgressMap = this.maxChapterReadingProgressMap.toMutableMap()
        currentChapterReadingProgressMap[chapterId] = progress
        maxChapterReadingProgressMap[chapterId] = maxProgress
        return this.copy(
            currentChapterReadingProgressMap = currentChapterReadingProgressMap,
            maxChapterReadingProgressMap = maxChapterReadingProgressMap
        )
    }
}