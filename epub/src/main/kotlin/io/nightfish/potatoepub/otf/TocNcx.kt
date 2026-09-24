package io.nightfish.potatoepub.otf

import io.nightfish.potatoepub.xml.Version
import io.nightfish.potatoepub.xml.WriteToZipAble
import io.nightfish.potatoepub.xml.XmlBuilder
import io.nightfish.potatoepub.xml.XmlBuilder.Companion.xml
import io.nightfish.potatoepub.xml.asFormatedXml
import java.util.zip.ZipEntry

/**
 * This is to adapt to the classes that NCX added in opf-201 to add cover.
 */
data class TocNcx(
    val uid: String,
    val title: String,
    val navPoints: List<NavPoint>
) : WriteToZipAble {
    override val zipEntry = ZipEntry("EPUB/toc.ncx")
    override fun toByteArray(): ByteArray =
        xml(
            "ncx",
            "http://www.daisy.org/z3986/2005/ncx/",
            Version("2005-1")
        ) {
            "head" {
                "meta"(
                    "content" to uid,
                    "name" to "dtb:uid"
                )
            }
            "docTitle" {
                "text" { title }
            }
            "navMap" {
                navPoints.forEach { it.element(this) }
            }
        }.asFormatedXml().toByteArray(Charsets.UTF_8)

    data class NavPoint(
        val id: String,
        val label: String,
        val content: String,
        val navPoints: List<NavPoint>? = null
    ) {
        fun element(builder: XmlBuilder.ElementBuilder) {
            builder.apply {
                "navPoint"("id" to id) {
                    "navLabel" {
                        "text" { label }
                    }
                    "content"("src" to content)
                    navPoints?.forEach { it.element(this) }
                    Any()
                }
            }
        }
    }
}