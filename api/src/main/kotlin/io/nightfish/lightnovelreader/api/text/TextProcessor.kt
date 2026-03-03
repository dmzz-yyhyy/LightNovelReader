package io.nightfish.lightnovelreader.api.text

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.content.component.SimpleTextComponentData
import io.nightfish.lightnovelreader.api.explore.ExploreDisplayBook

interface TextProcessor {
    val enabled: Boolean

    fun processText(text: String): String
    fun List<String>.process() = this.map(::processText)
    fun <T> Map<T, String>.process() = this.mapValues { (_, text) ->
        processText(text)
    }
    fun processBookInformation(bookInformation: BookInformation): BookInformation = bookInformation.toMutable().apply {
        this.title = processText(title)
        this.subtitle = processText(subtitle)
        this.author = processText(author)
        this.description = processText(description)
        this.publishingHouse = processText(publishingHouse)
    }
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
    fun processChapterContent(bookId: String, chapterContent: ChapterContent, componentProcessor: ComponentProcessor): ChapterContent = chapterContent.toMutable().apply {
        this.content = componentProcessor.apply {
            process<SimpleTextComponentData> {
                SimpleTextComponentData(processText(it.text))
            }
        }.get()
    }
    fun processExploreBooksRow(exploreDisplayBook: ExploreDisplayBook): ExploreDisplayBook = exploreDisplayBook.copy(
        title = this.processText(exploreDisplayBook.title),
        author = this.processText(exploreDisplayBook.author),
    )
}