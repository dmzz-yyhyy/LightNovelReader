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