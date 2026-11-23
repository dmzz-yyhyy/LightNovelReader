package io.nightfish.lightnovelreader.api.text.font

import android.net.Uri
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import io.nightfish.lightnovelreader.api.serializer.FontStyleSerializer
import io.nightfish.lightnovelreader.api.serializer.FontWeightSerializer
import io.nightfish.lightnovelreader.api.serializer.UriSerializer
import kotlinx.serialization.Serializable

/**
 * 本地字体配置
 *
 * @param uri 本地字体文件Uri
 * @param weight 字体粗细
 * @param style 字体样式
 * @param variationSettings 可变字体轴配置, null表示使用默认配置
 *
 * @since Api 4
 */
@Serializable
data class FontConfig(
    @Serializable(UriSerializer::class)
    val uri: Uri,
    @Serializable(FontWeightSerializer::class)
    val weight: FontWeight = FontWeight.Normal,
    @Serializable(FontStyleSerializer::class)
    val style: FontStyle = FontStyle.Normal,
    val variationSettings: FontVariationConfig? = null
)