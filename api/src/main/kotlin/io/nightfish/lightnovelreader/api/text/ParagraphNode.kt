package io.nightfish.lightnovelreader.api.text

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

/**
 * 段落节点, 按顺序组织文本节点与段落样式
 *
 * @param textNodes 段落内的文本节点集合
 * @param style 段落样式
 *
 * @since Api 4
 */
@Stable
@Serializable
data class ParagraphNode(
    val textNodes: Collection<TextNode> = emptyList(),
    val style: ParagraphStyle = ParagraphStyle()
)