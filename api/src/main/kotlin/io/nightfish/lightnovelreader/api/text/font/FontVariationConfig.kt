package io.nightfish.lightnovelreader.api.text.font

import kotlinx.serialization.Serializable

@Serializable
data class FontVariationConfig(
    val settings: List<VariationItem>
)