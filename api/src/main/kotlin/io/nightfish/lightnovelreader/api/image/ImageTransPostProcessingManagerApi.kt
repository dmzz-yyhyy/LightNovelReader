package io.nightfish.lightnovelreader.api.image

import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * 图片后处理管道管理器
 *
 * @since Api 4
 */
interface ImageTransPostProcessingManagerApi {
    /**
     * 注册图片后处理管道
     *
     * @param identifier 管道标识符
     *
     * @since Api 4
     */
    fun registerImagePostProcessPipeline(identifier: Identifier)

    /**
     * 注册图片转换器
     *
     * @param pipeline 目标管道标识符
     * @param transformationIdentifier 转换器标识符
     * @param transformation 图片转换器实例
     *
     * @since Api 4
     */
    fun registerImageTransformation(
        pipeline: Identifier,
        transformationIdentifier: Identifier,
        transformation: ImageTransformation
    )

    /**
     * 获取某个管道下全部的转换器
     *
     * @param pipeline 处理管道标识符
     *
     * @since Api 4
     */
    fun getImageTransformations(pipeline: Identifier): Collection<Pair<Identifier, ImageTransformation>>?

    /**
     * 获取某个图片转换器实例
     *
     * @param pipeline 处理管道标识符
     * @param transformation 图片转换器标识符
     *
     * @since Api 4
     */
    fun getImageTransformation(pipeline: Identifier, transformation: Identifier): ImageTransformation?

    /**
     * 取消注册图片转换器
     *
     * @param pipeline 处理管道标识符
     * @param transformation 图片转换器标识符
     *
     * @since Api 4
     */
    fun unregisterTransformations(pipeline: Identifier, transformation: Identifier)
}