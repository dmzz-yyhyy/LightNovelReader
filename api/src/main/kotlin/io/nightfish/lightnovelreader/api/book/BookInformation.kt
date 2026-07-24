package io.nightfish.lightnovelreader.api.book

import android.net.Uri
import androidx.compose.runtime.Stable
import java.time.LocalDateTime

/**
 * 书本详情接口
 *
 * @property id 书本id
 * @property title 书本标题
 * @property subtitle 书本副标题，如果没有则为空字符串
 * @property coverUri 书本封面的[Uri]
 * @property author 书本作者
 * @property description 书本简介
 * @property tags 书本的标签列表
 * @property publishingHouse 书本出版社
 * @property wordCount 书本字数信息
 * @property lastUpdated 书本最后更新时间
 * @property isComplete 书本是否已完结
 *
 * @since Api 4
 */
@Stable
data class BookInformation(
    val id: String,
    val title: String,
    val subtitle: String,
    val coverUri: Uri,
    val author: String,
    val description: String,
    val tags: List<String>,
    val publishingHouse: String,
    val wordCount: WordCount,
    val lastUpdated: LocalDateTime,
    val isComplete: Boolean
)