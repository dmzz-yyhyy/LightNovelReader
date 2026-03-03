package io.nightfish.potatoepub.xml

import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.io.OutputFormat
import org.dom4j.io.XMLWriter
import java.io.StringWriter


fun Document.asFormatedXml(): String {
    val format = OutputFormat()
    format.encoding = "UTF-8"
    format.isNewlines = true
    format.indent = "  "
    format.isExpandEmptyElements = false
    val strWtr = StringWriter()
    val xmlWrt = XMLWriter(strWtr, format)
    xmlWrt.write(DocumentHelper.parseText(sanitizeXmlSimple(this.asXML())))
    xmlWrt.flush()
    xmlWrt.close()
    return strWtr.toString()
        .replaceFirst(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        )
}

fun sanitizeXmlSimple(xml: String): String =
    xml
        .replace(Regex("&#(?:x[0-9a-fA-F]+|\\d+);")) { m ->
            val s = m.value
            val cp = if (s.startsWith("&#x", ignoreCase = true)) {
                s.substring(3, s.length - 1).toInt(16)
            } else {
                s.substring(2, s.length - 1).toInt()
            }
            val ok = (cp == 0x9 || cp == 0xA || cp == 0xD) ||
                    (cp in 0x20..0xD7FF) ||
                    (cp in 0xE000..0xFFFD)
            if (ok) m.value else ""
        }
        .replace(Regex("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD]"), "")