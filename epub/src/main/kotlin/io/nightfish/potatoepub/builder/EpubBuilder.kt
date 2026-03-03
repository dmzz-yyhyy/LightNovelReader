package io.nightfish.potatoepub.builder

import io.nightfish.potatoepub.Epub
import io.nightfish.potatoepub.otf.EpubManifest
import io.nightfish.potatoepub.otf.Metadata
import io.nightfish.potatoepub.otf.Nav
import io.nightfish.potatoepub.otf.OpfPackage
import io.nightfish.potatoepub.otf.Spine
import io.nightfish.potatoepub.otf.TocNcx
import io.nightfish.potatoepub.otf.metaInf.Container
import io.nightfish.potatoepub.xml.TextDirection
import org.dom4j.Document
import java.io.File
import java.time.LocalDateTime
import java.util.Locale

class EpubBuilder {
    var id: String? = null
    var title: String? = null
    var modifier: LocalDateTime? = null
    var titleLang: Locale = Locale.ENGLISH
    var titleDir: TextDirection = TextDirection.LTR
    var language: Locale = Locale.ENGLISH
    var creator: String? = null
    var description: String? = null
    var publisher: String? = null
    var manifestId: String? = null
    var manifestItems: MutableSet<EpubManifest.Item> = mutableSetOf(
        EpubManifest.Item(href = "toc.ncx", id = "ncx", mediaType = "application/x-dtbncx+xml"),
        EpubManifest.Item(href = "nav.xhtml", id = "nav", mediaType = "application/xhtml+xml", properties = "nav"),
        EpubManifest.Item(href = "cover.jpg", id = "cover", mediaType = "image/jpeg", properties = "cover-image"),
    )
    var spineId: String? = null
    var spineItems: MutableList<Spine.Itemref> = mutableListOf()
    var hasCover: Boolean = false
    var chapters: MutableList<Chapter> = mutableListOf()
    var contentChapters: MutableList<Chapter> = mutableListOf()
    var resFiles: MutableMap<String, File> = mutableMapOf()
    var documents: MutableMap<String, Document> = mutableMapOf()

    fun chapter(builder: ChapterBuilder.() -> Unit) {
        val chapter = ChapterBuilder().let { chapterBuilder ->
            builder.invoke(chapterBuilder)
            val chapter = chapterBuilder.build()
            chapterBuilder.contentBuilders.forEach { simpleContentBuilder ->
                simpleContentBuilder.images.forEach {
                    manifestItems.add(
                        EpubManifest.Item(
                            href = it.key.second,
                            id = it.key.first,
                            mediaType = "image/jpeg",
                        )
                    )
                    resFiles[it.key.second] = it.value
                }
            }
            chapter
        }
        chapters.add(chapter)
    }

    fun imgRes(
        href: String,
        id: String,
        file: File
    ) {
        manifestItems.add(
            EpubManifest.Item(
                href = href,
                id = id,
                mediaType = "image/jpeg",
            )
        )
        resFiles[href] = file
    }

    fun res(
        id: String,
        path: String,
        mediaType: String,
        file: File,
        properties: String? = null,
        mediaOverride: String? = null,
        fallback: String? = null
    ) {
        resFiles[path] = file
        manifestItems.add(EpubManifest.Item(
            href = path,
            id = id,
            mediaType = mediaType,
            mediaOverride = mediaOverride,
            fallback = fallback,
            properties = properties)
        )
    }

    /**
     * the cover file must be jpg
     */
    fun cover(file: File) {
        hasCover = true
        res(
            id = "cover",
            path = "cover.jpg",
            mediaType = "image/jpeg",
            properties = "cover-image",
            file = file
        )
    }

    private fun List<Chapter>.toOl(): Nav.Ol = Nav.Ol(
        this.map {
            Nav.Li(
                title = it.title,
                href = if (it.chapterContent != null) {
                    contentChapters.add(it)
                    "${it.id}.xhtml"
                } else null,
                ol = it.chapters?.toOl()
            )
        }
    )

    private fun Chapter.toNavPoint(): TocNcx.NavPoint {
        if (this.chapterContent != null)
            return TocNcx.NavPoint(
                id = this.id,
                label = this.title,
                content = "${this.id}.xhtml"
            )
        this.chapters!!
        if (this.chapters.isEmpty()) throw Error("TheChapterList is empty")
        fun List<Chapter>.fistChapterWithContent(): Chapter {
            return if (this.first().chapterContent != null)
                this.first()
            else
                this.first().chapters!!.fistChapterWithContent()
        }
        val firstChapter = this.chapters.fistChapterWithContent()
        return TocNcx.NavPoint(
            id = "sep_${this.id}",
            label = this.title,
            navPoints = this.chapters.map { it.toNavPoint() },
            content = "${firstChapter.id}.xhtml"
        )
    }

    private fun checkXmlFileHref() {
        for (chapter in this.contentChapters) {
            val xml = chapter.chapterContent!!.asXML()
            val regex1 = Regex("src=\"(.*?)\"")
            regex1.findAll(xml).toList().forEach {
                if (!resFiles.containsKey(it.groupValues[1]))
                    throw Error("Didn't find res '${it.groupValues[1]}' which appear in file {${chapter.title}}. Pleas make sure use method 'res' to add the res into the EPUB.")
            }
            val regex2 = Regex("herf=\"(.*?)\"")
            regex2.findAll(xml).toList().forEach {
                if (!resFiles.containsKey(it.groupValues[1]))
                    throw Error("Didn't find res '${it.groupValues[1]}' which appear in file {${chapter.title}}. Pleas make sure use method 'res' to add the res into the EPUB.")
            }
        }
    }

    fun build(): Epub {
        id = id ?: title
        val ol = chapters.toOl()
        val navPoints = chapters.map { it.toNavPoint() }
        val container = Container(
            rootFilePaths = listOf("EPUB/content.opf")
        )
        manifestItems.addAll(contentChapters.map {
            EpubManifest.Item(
                href = it.id + ".xhtml",
                id = it.id,
                mediaType = "application/xhtml+xml"
            )
        })
        spineItems.addAll(contentChapters.map {
            Spine.Itemref(idref = it.id)
        })
        contentChapters.forEach {
            documents["${it.id}.xhtml"] = it.chapterContent!!
        }
        checkXmlFileHref()
        val metadata = Metadata(
            id = id ?: throw Error("Missing 'id'"),
            title = title ?: throw Error("Missing 'title'"),
            titleLang = titleLang,
            titleDir = titleDir,
            language = language,
            modified = modifier ?: throw Error("Missing 'modifier'"),
            coverId = if (hasCover) "cover" else null,
            creator = creator,
            description = description,
            publisher = publisher
        )
        val manifest = EpubManifest(
            id = manifestId,
            items = manifestItems.toList()
        )
        val spine = Spine(
            id = spineId,
            itemrefList = spineItems
        )
        val opfPackage = OpfPackage(
            metadata = metadata,
            manifest = manifest,
            spine = spine
        )
        val nav = Nav(
            title = id ?: throw Error("Missing 'id'"),
            ol = ol
        )
        val tocNcx = TocNcx(
            uid = id ?: throw Error("Missing 'id'"),
            title = title ?: throw Error("Missing 'title'"),
            navPoints = navPoints,
        )
        return Epub(
            container = container,
            opfPackage = opfPackage,
            nav = nav,
            tocNcx = tocNcx,
            res = resFiles,
            documents = documents
        )
    }
}