package io.nightfish.lightnovelreader.api.text.font

import kotlinx.serialization.Serializable

/**
 * 可变字体的单个轴配置
 * 根据数值类型保存字体轴的取值
 *
 * @since Api 4
 */
@Serializable
sealed class VariationItem {

    /**
     * 字体轴标记, 如wght、ital或opsz
     *
     * @since Api 4
     */
    abstract val axis: String

    /**
     * 使用浮点数保存的字体轴配置
     *
     * @param axis 字体轴标记
     * @param value 字体轴取值
     *
     * @since Api 4
     */
    @Serializable
    data class FloatValue(
        override val axis: String,
        val value: Float
    ) : VariationItem()

    /**
     * 使用整数保存的字体轴配置
     *
     * @param axis 字体轴标记
     * @param value 字体轴取值
     *
     * @since Api 4
     */
    @Serializable
    data class IntValue(
        override val axis: String,
        val value: Int
    ) : VariationItem()

    /**
     * 使用带单位的文本保存的字体轴配置
     *
     * @param axis 字体轴标记
     * @param value 字体轴取值, 以sp或em后缀保留单位
     *
     * @since Api 4
     */
    @Serializable
    data class TextUnitValue(
        override val axis: String,
        val value: String
    ) : VariationItem()
}