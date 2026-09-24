package io.nightfish.potatoepub.otf.metaInf

import io.nightfish.potatoepub.xml.Version
import io.nightfish.potatoepub.xml.WriteToZipAble
import io.nightfish.potatoepub.xml.XmlBuilder.Companion.xml
import io.nightfish.potatoepub.xml.asFormatedXml
import java.util.zip.ZipEntry

data class Container(val rootFilePaths: List<String>) : WriteToZipAble {
    override val zipEntry: ZipEntry = ZipEntry("META-INF/container.xml")
    override fun toByteArray(): ByteArray =
        xml(
            "container",
            "urn:oasis:names:tc:opendocument:xmlns:container",
            Version("1.0")
        ) {
            "rootfiles" {
                for (path in rootFilePaths) {
                    "rootfile"(
                        "media-type" to "application/oebps-package+xml",
                        "full-path" to path
                    )
                }
            }
        }.asFormatedXml().toByteArray(Charsets.UTF_8)
}