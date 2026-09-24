package io.nightfish.lightnovelreader.api.text

import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class TextNode(
    val text: String,
    val style: TextStyle = TextStyle()
)