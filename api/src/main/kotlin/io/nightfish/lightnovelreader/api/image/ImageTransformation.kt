package io.nightfish.lightnovelreader.api.image

import android.graphics.Bitmap
import android.net.Uri

/**
 * 图片转换器接口
 *
 * @since Api 4
 */
interface ImageTransformation {
    /**
     * 仅根据元数据计算后处理后的尺寸，不读取或解码图片像素。
     * 默认保持原尺寸；会改变宽高的转换器必须重写此方法，与[transform]保持一致。
     *
     * @param input 原图或前一个转换器输出的尺寸
     * @param size 图片被设置显示的大小，与[transform]的size含义相同
     * @param uri 输入图片来源Uri
     *
     * @return 后处理后的图片尺寸
     *
     * @since Api 4
     */
    fun measure(input: ImageSize, size: ImageSize, uri: Uri): ImageSize = input

    /**
     * 图片处理后的缓存key
     * 需要确保同一图片在相同key下是一样的
     *
     * @param uri 输入图片来源Uri
     *
     * @since Api 4
     */
    fun getCacheKey(uri: Uri): String

    /**
     * 处理函数
     *
     * @param input 输入图片
     * @param size 输入图片尺寸(单位为像素)
     * @param uri 输入图片来源Uri
     *
     * @return 输出图片
     *
     * @since Api 4
     */
    suspend fun transform(input: Bitmap, size: ImageSize, uri: Uri): Bitmap
}
