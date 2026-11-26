package io.nightfish.lightnovelreader.api.content.component

/**
 * 书写方向
 *
 * @since Api 4
 */
enum class WriteMode {
    /**
     * 普通水平书写方向
     */
    Horizontal,

    /**
     * 垂直从右至左书写方向
     */
    VerticalRTL,

    /**
     * 垂直从左至右书写方向
     */
    VerticalLTR
}