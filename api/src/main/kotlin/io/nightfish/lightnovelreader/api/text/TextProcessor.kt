package io.nightfish.lightnovelreader.api.text

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.content.component.SimpleTextComponentData
import io.nightfish.lightnovelreader.api.explore.ExploreDisplayBook

/**
 * 文本处理器接口
 * 实现此接口可对软件中各类文本数据进行统一转换处理（如繁简转换）
 * 注册后软件会在数据流经过时自动调用对应方法
 *
 * @since Api 2
 */
interface TextProcessor {
    /**
     * 此处理器是否启用
     * 为false时软件将跳过该处理器的所有处理逻辑
     *
     * @since Api 2
     */
    val enabled: Boolean

    /**
     * 对单个字符串进行文本转换处理
     *
     * @param text 需要处理的原始文本
     *
     * @return 处理后的文本
     *
     * @since Api 2
     */
    fun processText(text: String): String

    /**
     * 对字符串列表中的每个元素进行文本处理
     *
     * @return 处理后的字符串列表
     *
     * @since Api 2
     */
    fun List<String>.process() = this.map(::processText)

    /**
     * 对Map中所有String类型的值进行文本处理
     *
     * @return value已处理的Map
     *
     * @since Api 2
     */
    fun <T> Map<T, String>.process() = this.mapValues { (_, text) ->
        processText(text)
    }

    /**
     * 对书本详情中的文本字段进行处理
     * 会处理标题、副标题、作者、简介、出版社字段
     *
     * @param bookInformation 需要处理的书本详情
     *
     * @return 处理后的书本详情
     *
     * @since Api 2
     */
    fun processBookInformation(bookInformation: BookInformation): BookInformation = bookInformation.copy(
        title = processText(bookInformation.title),
        subtitle = processText(bookInformation.subtitle),
        author = processText(bookInformation.author),
        description = processText(bookInformation.description),
        publishingHouse = processText(bookInformation.publishingHouse)
    )

    /**
     * 对书本卷目录中的文本字段进行处理
     * 会处理卷标题及各章节标题字段
     *
     * @param bookVolumes 需要处理的书本卷目录
     *
     * @return 处理后的书本卷目录
     *
     * @since Api 2
     */
    fun processBookVolumes(bookVolumes: BookVolumes): BookVolumes = bookVolumes.copy(
        volumes = bookVolumes.volumes.map { volume ->
            volume.copy(
                volumeTitle = processText(volume.volumeTitle),
                chapters = volume.chapters.map {
                    it.copy(
                        title = processText(it.title)
                    )
                }
            )
        })

    /**
     * 对章节内容中的文本组件进行处理
     *
     * @param bookId 章节所属书本id
     * @param chapterContent 需要处理的章节内容
     * @param componentProcessor 用于遍历并修改章节内容组件的处理器
     *
     * @return 处理后的章节内容
     *
     * @since Api 2
     */
    fun processChapterContent(bookId: String, chapterContent: ChapterContent, componentProcessor: ComponentProcessor): ChapterContent = chapterContent.copy(
        content = componentProcessor.apply {
            process<SimpleTextComponentData> {
                SimpleTextComponentData(processText(it.text))
            }
        }.get()
    )

    /**
     * 对探索页书本简要信息中的文本字段进行处理
     * 会处理标题和作者字段
     *
     * @param exploreDisplayBook 需要处理的探索页书本信息
     *
     * @return 处理后的探索页书本信息
     *
     * @since Api 2
     */
    fun processExploreBooksRow(exploreDisplayBook: ExploreDisplayBook): ExploreDisplayBook = exploreDisplayBook.copy(
        title = this.processText(exploreDisplayBook.title),
        author = this.processText(exploreDisplayBook.author),
    )
}