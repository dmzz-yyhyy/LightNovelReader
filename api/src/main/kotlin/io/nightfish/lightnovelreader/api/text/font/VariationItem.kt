package io.nightfish.lightnovelreader.api.text.font

import kotlinx.serialization.Serializable

@Serializable
sealed class VariationItem {

    abstract val axis: String

    @Serializable
    data class FloatValue(
        override val axis: String,
        val value: Float
    ) : VariationItem()

    @Serializable
    data class IntValue(
        override val axis: String,
        val value: Int
    ) : VariationItem()

    @Serializable
    data class TextUnitValue(
        override val axis: String,
        val value: String
    ) : VariationItem()
}