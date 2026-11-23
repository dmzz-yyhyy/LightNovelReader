package io.nightfish.lightnovelreader.api.text

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

/**
 * 文本节点, 保存文本内容及其局部样式
 *
 * @param text 文本内容
 * @param style 文本样式, 未指定的属性由段落或阅读器样式决定
 *
 * @since Api 4
 */
@Stable
@Serializable
data class TextNode(
    val text: String,
    val style: TextStyle = TextStyle()
)