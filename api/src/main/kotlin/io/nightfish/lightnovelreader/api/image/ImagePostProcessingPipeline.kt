package io.nightfish.lightnovelreader.api.image

import io.nightfish.lightnovelreader.api.identifier.ofAppId

/**
 * 软件内默认的图像后处理管道
 *
 * @since Api 4
 */
object ImagePostProcessingPipeline {
    private fun ofId(id: String) = "image_post_processing_pipeline:$id".ofAppId()

    /**
     * 阅读器图片控件与图片浏览页面所使用的后处理器管道
     */
    val imageComponent = ofId("image_component")

    /**
     * 书本封面使用的后处理管道
     */
    val bookCover = ofId("book_cover")
}