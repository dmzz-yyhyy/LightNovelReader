package io.nightfish.lightnovelreader.api.text

import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import io.nightfish.lightnovelreader.api.serializer.TextIndentSerializer
import io.nightfish.lightnovelreader.api.serializer.TextUnitSerializer
import kotlinx.serialization.Serializable

/**
 * 段落样式配置
 * 未指定的属性由阅读器样式或基底样式决定
 *
 * @param lineHeight 行高, 竖排时用于确定列宽
 * @param textIndent 首行与其余行的缩进, null表示未指定
 * @param spacingBefore 段前间距
 * @param spacingAfter 段后间距
 * @param textStyle 段落内文本的基础样式, null表示未指定
 *
 * @since Api 4
 */
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
    /**
     * 转换为Compose段落样式
     * 段前、段后间距与文本样式由组件渲染器单独处理
     *
     * @return 包含行高与缩进的Compose段落样式
     *
     * @since Api 4
     */
    fun toParagraphStyle() = ParagraphStyle(
        lineHeight = lineHeight,
        textIndent = textIndent,
    )
}
