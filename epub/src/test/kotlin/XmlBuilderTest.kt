import io.nightfish.potatoepub.xml.Attribute
import io.nightfish.potatoepub.xml.XmlBuilder.Companion.xml
import io.nightfish.potatoepub.xml.asFormatedXml
import org.dom4j.DocumentHelper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class XmlBuilderTest {
    @Test
    fun formatsNestedElementsWithDoctype() {
        val document = xml("html") {
            "head" {
                "title" { "Test book" }
            }
            "body" {
                "p" { "Chapter text" }
                "br"()
            }
        }.addDocType("html", "", "")

        val formatted = document.asFormatedXml().replace("\r\n", "\n")

        assertEquals(
            """
            <?xml version="1.0" encoding="UTF-8"?><!DOCTYPE html>

            <html>
              <head>
                <title>Test book</title>
              </head>
              <body>
                <p>Chapter text</p>
                <br/>
              </body>
            </html>
            """.trimIndent(),
            formatted.trimEnd()
        )
    }

    @Test
    fun preservesNamespacesAndEscapesTextAndAttributes() {
        val namespace = "http://www.w3.org/1999/xhtml"
        val text = "中文章节 <one> & two"
        val href = "chapter.xhtml?first=1&second=2"
        val document = xml("html", namespace, Attribute("lang", "zh-CN")) {
            "body" {
                "a"("href" to href, "title" to null) { text }
            }
        }

        val formatted = document.asFormatedXml()
        val root = DocumentHelper.parseText(formatted).rootElement
        val link = root.element("body").element("a")

        assertEquals(namespace, root.namespaceURI)
        assertEquals(namespace, link.namespaceURI)
        assertEquals("zh-CN", root.attributeValue("lang"))
        assertEquals(text, link.text)
        assertEquals(href, link.attributeValue("href"))
        assertNull(link.attribute("title"))
        assertTrue(formatted.contains("&lt;one&gt; &amp; two"))
        assertTrue(formatted.contains("first=1&amp;second=2"))
    }
}
