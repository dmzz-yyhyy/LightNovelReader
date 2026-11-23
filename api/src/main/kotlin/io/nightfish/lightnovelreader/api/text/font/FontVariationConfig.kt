package io.nightfish.lightnovelreader.api.text.font

import kotlinx.serialization.Serializable

/**
 * 可变字体配置
 *
 * @param settings 字体变体轴配置列表
 *
 * @since Api 4
 */
@Serializable
data class FontVariationConfig(
    val settings: List<VariationItem>
)