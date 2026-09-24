package io.nightfish.lightnovelreader.api.text

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import io.nightfish.lightnovelreader.api.serializer.TextIndentSerializer
import io.nightfish.lightnovelreader.api.serializer.TextUnitSerializer
import kotlinx.serialization.Serializable

@Serializable
data class ParagraphStyle(
    @Serializable(TextUnitSerializer::class)
    val lineHeight: TextUnit = TextUnit.Unspecified,
    @Serializable(TextIndentSerializer::class)
    val textIndent: TextIndent? = null,
    @Serializable(TextUnitSerializer::class)
    val spacingBefore: TextUnit = TextUnit.Unspecified,
    @Serializable(TextUnitSerializer::class)
    val spacingAfter: TextUnit = TextUnit.Unspecified,
    val textStyle: TextStyle? = null
) {
    fun toParagraphStyle() = ParagraphStyle(
        lineHeight = lineHeight,
        textIndent = textIndent,
    )
}