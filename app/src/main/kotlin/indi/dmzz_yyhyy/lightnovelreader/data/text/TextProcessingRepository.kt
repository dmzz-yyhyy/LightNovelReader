package indi.dmzz_yyhyy.lightnovelreader.data.text

import indi.dmzz_yyhyy.lightnovelreader.data.content.ContentComponentRepository
import indi.dmzz_yyhyy.lightnovelreader.data.format.FormatRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.ofId
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.explore.ExploreDisplayBook
import io.nightfish.lightnovelreader.api.text.ComponentProcessor
import io.nightfish.lightnovelreader.api.text.TextProcessingRepositoryApi
import io.nightfish.lightnovelreader.api.text.TextProcessor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextProcessingRepository @Inject constructor(
    simplifiedTraditionalProcessor: SimplifiedTraditionalProcessor,
    formatRepository: FormatRepository,
    val contentComponentRepository: ContentComponentRepository
): TextProcessingRepositoryApi {
    private val processors = mutableMapOf<Identifier, TextProcessor>()

    override fun registerProcessors(identifier: Identifier, processor: TextProcessor) {
        if (processors.contains(identifier)) return
        processors[identifier] = processor
    }

    private fun <T> process(t: T, block: (TextProcessor) -> ((T) -> T)): T {
        val processors = this.processors
            .filterValues { it.enabled }
        var result = t
        for (processor in processors.values) {
            result = block.invoke(processor).invoke(result)
        }
        return result
    }

    fun processText(block: () -> String): String = process(block.invoke()) { it::processText }
    fun processBookInformation(block: () -> BookInformation): BookInformation = process(block.invoke()) { it::processBookInformation }
    suspend fun coroutineProcessBookInformation(block: suspend () -> BookInformation): BookInformation = process(block.invoke()) { processor ->
        {
            processor.processBookInformation(it)
        }
    }

    fun processBookVolumes(block: () -> BookVolumes): BookVolumes = process(block.invoke()) { it::processBookVolumes }
    fun processChapterContent(bookId: String, block: () -> ChapterContent): ChapterContent = process(block.invoke()) { processor ->
        {
            processor.processChapterContent(bookId, it, ComponentProcessor(
                contentComponentRepository.serializeMap, contentComponentRepository.dataKClassMap, it.content
            ))
        }
    }

    suspend fun coroutineProcessChapterContent(bookId: String, block: suspend () -> ChapterContent): ChapterContent = process(block.invoke()) { processor ->
        {
            processor.processChapterContent(bookId, it, ComponentProcessor(
                contentComponentRepository.serializeMap, contentComponentRepository.dataKClassMap, it.content
            ))
        }
    }
    fun processExploreBooksRow(exploreDisplayBook: ExploreDisplayBook): ExploreDisplayBook = process(exploreDisplayBook) { it::processExploreBooksRow }

    init {
        registerProcessors("simplified_traditional_processor".ofId(), simplifiedTraditionalProcessor)
        registerProcessors("format_repository".ofId(), formatRepository)
    }
}