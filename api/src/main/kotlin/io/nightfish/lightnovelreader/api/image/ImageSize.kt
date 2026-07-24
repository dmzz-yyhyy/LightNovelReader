package io.nightfish.lightnovelreader.api.image

/**
 * 用于图片转换器得传参类型
 * 记录图片被设置显示的大小
 * 如果值为null则表示未指定该方向的大小
 *
 * @param width 图片宽度
 * @param height 图片高度
 *
 * @since Api 4
 */
data class ImageSize(
    val width: Int?,
    val height: Int?
)
