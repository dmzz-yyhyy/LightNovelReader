package io.nightfish.potatoepub.builder

import io.nightfish.potatoepub.xml.Attribute
import io.nightfish.potatoepub.xml.XmlBuilder
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element
import java.io.File

@Suppress("MemberVisibilityCanBePrivate")
class SimpleContentBuilder {
    private val _images: MutableMap<Pair<String, String>, File> = mutableMapOf()
    val images: Map<Pair<String, String>, File> get() = _images
    val document: Document = DocumentHelper.createDocument()
    val rootElement: Element = document.addElement("html", "http://www.w3.org/1999/xhtml")
    val headElement: Element = rootElement.addElement("head")
    val bodyElement: Element = rootElement.addElement("body")
    val contentElement: Element = bodyElement.addElement("div").addAttribute("id", "content")

    init {
        document.addDocType("html", "", "")
        rootElement
            .addAttribute("xmlns", "http://www.w3.org/1999/xhtml")
            .addAttribute("xmlns:epub", "http://www.idpf.org/2007/ops")
            .addAttribute("lang", "en")
            .addAttribute("xml:lang", "en")
    }

    fun title(src: String) {
        headElement.addElement("title").addText(src)
    }

    fun headline(level: Int, content: String) {
        contentElement.addElement("h$level").addText(content)
    }

    fun br() {
        contentElement.addElement("br")
    }

    fun text(content: String) {
        var result = Regex("&#([0-8]|1[1-2]|1[4-9]|2[0-9]|3[0-1]);").replace(content, "")
        result = Regex(
            "&#x(0[0-8BCEF]|1[0-9A-F]|7F|8[0-9A-F]|9[0-9A-F]|A[0-9A-F]|B[0-9A-F]|C[0-9A-F]|D[0-9A-F]|E[0-9A-F]|F[0-9A-F]);",
            RegexOption.IGNORE_CASE
        )
            .replace(result, "")
        contentElement.addText(result)
    }

    /**
     * make sure image is jpeg file
     */
    fun image(
        image: File,
        id: String = "image_${image.hashCode()}",
        src: String = "image/$id.jpg"
    ) {
        _images[Pair(id, src)] = image
        XmlBuilder.ElementBuilder(contentElement, "div", arrayOf(Attribute("class", "div_image"))) {
            "img"(
                "border" to 0,
                "class" to "image_content",
                "src" to src
            )
        }
    }

    fun build(): Document {
        return document
    }
}