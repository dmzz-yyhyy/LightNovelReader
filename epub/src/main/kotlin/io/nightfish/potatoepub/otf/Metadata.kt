package io.nightfish.potatoepub.otf

import io.nightfish.potatoepub.xml.Attribute
import io.nightfish.potatoepub.xml.TextDirection
import io.nightfish.potatoepub.xml.XmlBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class Metadata(
    val id: String,
    val identifierType: String? = null,
    val title: String,
    val titleLang: Locale = Locale.ENGLISH,
    val titleDir: TextDirection = TextDirection.LTR,
    val language: Locale = Locale.ENGLISH,
    val modified: LocalDateTime,
    val creator: String? = null,
    val description: String? = null,
    val publisher: String? = null,
    //This is to adapt to the meta element added by adding cover in opf-201
    val coverId: String?
) {
    companion object {
        val dataTimeFormat: DateTimeFormatter =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'")
    }

    fun element(builder: XmlBuilder.ElementBuilder) {
        builder.apply {
            "metadata"(
                "xmlns:dc" to "http://purl.org/dc/elements/1.1/",
                "xmlns:opt" to "http://www.idpf.org/2007/opf"
            ) {
                "dc:identifier"("id" to "pub-id") { id }
                if (identifierType != null)
                    "dc:identifier"("id" to "pub-id", "identifier-type" to identifierType) { id }
                "dc:title"(
                    "id" to "title",
                    "xml:lang" to titleLang,
                    titleDir,
                ) { title }
                "dc:language" { language.toString() }
                meta(
                    property = "dcterms:modified",
                    value = modified.format(dataTimeFormat)
                )
                creator?.let { "dc:creator" { creator } }
                description?.let { "dc:description" { description } }
                publisher?.let { "dc:publisher" { publisher } }
                //This is to adapt to the meta element added by adding cover in opf-201
                if (coverId != null)
                    "meta"(
                        "name" to "cover",
                        "content" to coverId,
                    )
            }
        }
    }

    private fun XmlBuilder.ElementBuilder.meta(
        dir: TextDirection? = null,
        id: String? = null,
        property: String,
        language: Locale? = null,
        value: String
    ) {
        "meta"(
            dir ?: Attribute.empty,
            "id" to id,
            "property" to property,
            "lang" to language,
        ) {
            value
        }
    }
}