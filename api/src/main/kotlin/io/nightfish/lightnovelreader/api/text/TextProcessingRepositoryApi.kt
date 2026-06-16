package io.nightfish.lightnovelreader.api.text

import io.nightfish.lightnovelreader.api.identifier.Identifier

/**
 * 文本处理相关的Api
 * 用于注册自定义[TextProcessor]文本处理器
 *
 * @since Api 2
 */
interface TextProcessingRepositoryApi {
    /**
     * 注册一个文本处理器
     * 注册后软件会在数据流通过时自动应用该处理器
     *
     * @param processor 需要注册的文本处理器实例
     *
     * @since Api 4
     */
    fun registerProcessors(identifier: Identifier, processor: TextProcessor)
}