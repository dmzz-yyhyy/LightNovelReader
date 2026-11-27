package io.nightfish.lightnovelreader.api.explore

import android.net.Uri
import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * 探索页用于展示的书本简要信息
 *
 * @param id 书本id
 * @param title 书本标题
 * @param author 书本作者
 * @param coverUri 书本封面的[Uri]
 *
 * @since Api 2
 */
data class ExploreDisplayBook(
    val id: Identifier.Book,
    val title: String,
    val author: String,
    val coverUri: Uri,
)
