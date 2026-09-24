package io.nightfish.lightnovelreader.api.text

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class ParagraphNode(
    val textNodes: Collection<TextNode> = emptyList(),
    val style: ParagraphStyle = ParagraphStyle()
)