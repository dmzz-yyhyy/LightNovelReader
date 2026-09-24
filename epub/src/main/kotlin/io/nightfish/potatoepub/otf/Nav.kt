package io.nightfish.potatoepub.otf

import io.nightfish.potatoepub.xml.Attribute
import io.nightfish.potatoepub.xml.WriteToZipAble
import io.nightfish.potatoepub.xml.XmlBuilder
import io.nightfish.potatoepub.xml.XmlBuilder.Companion.xml
import io.nightfish.potatoepub.xml.asFormatedXml
import java.util.Locale
import java.util.zip.ZipEntry

data class Nav(
    val language: Locale = Locale.ENGLISH,
    val title: String,
    val headline: Int = 2,
    val ol: Ol
) : WriteToZipAble {
    override val zipEntry: ZipEntry = ZipEntry("EPUB/nav.xhtml")
    override fun toByteArray(): ByteArray =
        xml(
            "html",
            "http://www.w3.org/1999/xhtml",
            Attribute("xmlns:epub", "http://www.idpf.org/2007/ops"),
            Attribute("lang", language.toString()),
            Attribute("xml:lang", language.toString()),
        ) {
            "head" {
                "title" { title }
            }
            "body" {
                "nav"(
                    "epub:type" to "toc",
                    "role" to "doc-toc"
                ) {
                    "h$headline" { title }
                    ol.element(this)
                }
            }
        }.asFormatedXml().toByteArray(Charsets.UTF_8)

    data class Li(
        val title: String,
        val href: String? = null,
        val ol: Ol? = null
    ) {
        fun element(builder: XmlBuilder.ElementBuilder) {
            builder.apply {
                "li" {
                    if (href == null)
                        "span" { title }
                    else
                        "a"("href" to href) { title }
                    ol?.element(this)
                    Any()
                }
            }
        }
    }

    data class Ol(
        val items: List<Li>
    ) {
        fun element(builder: XmlBuilder.ElementBuilder) {
            builder.apply {
                "ol" {
                    for (item in items) {
                        item.element(this)
                    }
                }
            }
        }
    }
}