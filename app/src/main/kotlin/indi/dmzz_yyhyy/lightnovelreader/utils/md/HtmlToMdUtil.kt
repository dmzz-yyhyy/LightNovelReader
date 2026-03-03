package indi.dmzz_yyhyy.lightnovelreader.utils.md

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Entities
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Tag
import org.jsoup.safety.Cleaner
import org.jsoup.safety.Safelist

object HtmlToMdUtil {
    private var indentation: Int = -1
    private var orderedList: Boolean = false

    fun convertHtml(html: String): String {
        val doc: Document = Jsoup.parse(html)

        return parseDocument(doc)
    }

    private fun parseDocument(dirtyDoc: Document): String {
        indentation = -1

        val title: String = dirtyDoc.title()

        val whitelist = Safelist.relaxed()
        val cleaner = Cleaner(whitelist)

        val doc: Document = cleaner.clean(dirtyDoc)
        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml)

        return if (title.trim { it <= ' ' } != "") {
            "# " + title + "\n\n" + getTextContent(doc)
        } else {
            getTextContent(doc)
        }
    }

    private fun getTextContent(element: Element): String {
        val lines: ArrayList<MDLine> = ArrayList()

        val children: List<Node> = element.childNodes()
        for (child: Node in children) {
            if (child is TextNode) {
                val textNode: TextNode = child
                val line: MDLine = getLastLine(lines)
                if (line.getContent() == "") {
                    if (!textNode.isBlank) {
                        line.append(
                            textNode.text().replace("#".toRegex(), "/#")
                                .replace("\\*".toRegex(), "/\\*")
                        )
                    }
                } else {
                    line.append(
                        textNode.text().replace("#".toRegex(), "/#")
                            .replace("\\*".toRegex(), "/\\*")
                    )
                }
            } else if (child is Element) {
                processElement(child, lines)
            }
        }

        var blankLines = 0
        val result: StringBuilder = StringBuilder()
        for (i in lines.indices) {
            val line: String = lines[i].toString().trim { it <= ' ' }
            if (line == "") {
                blankLines++
            } else {
                blankLines = 0
            }
            if (blankLines < 2) {
                result.append(line)
                if (i < lines.size - 1) {
                    result.append("\n")
                }
            }
        }

        return result.toString()
    }

    private fun processElement(element: Element, lines: ArrayList<MDLine>) {
        val tag: Tag = element.tag()

        val tagName: String = tag.name
        when {
            tagName == "div" -> {
                div(element, lines)
            }
            tagName == "p" -> {
                p(element, lines)
            }
            tagName == "br" -> {
                br(lines)
            }
            tagName.matches("^h[0-9]+$".toRegex()) -> {
                h(element, lines)
            }
            tagName == "strong" || tagName == "b" -> {
                strong(element, lines)
            }
            tagName == "em" -> {
                em(element, lines)
            }
            tagName == "hr" -> {
                hr(lines)
            }
            tagName == "a" -> {
                a(element, lines)
            }
            tagName == "img" -> {
                img(element, lines)
            }
            tagName == "code" -> {
                code(element, lines)
            }
            tagName == "ul" -> {
                ul(element, lines)
            }
            tagName == "ol" -> {
                ol(element, lines)
            }
            tagName == "li" -> {
                li(element, lines)
            }
            else -> {
                val line: MDLine = getLastLine(lines)
                line.append(getTextContent(element))
            }
        }
    }

    private fun getLastLine(lines: ArrayList<MDLine>): MDLine {
        val line: MDLine
        if (lines.isNotEmpty()) {
            line = lines[lines.size - 1]
        } else {
            line = MDLine(MDLine.MDLineType.None, 0, "")
            lines.add(line)
        }

        return line
    }

    private fun div(element: Element, lines: ArrayList<MDLine>) {
        val line: MDLine = getLastLine(lines)
        val content: String = getTextContent(element)
        if (content != "") {
            if (line.getContent().trim { it <= ' ' } != "") {
                lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
                lines.add(MDLine(MDLine.MDLineType.None, 0, content))
                lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
            } else {
                if (content.trim { it <= ' ' } != "") line.append(content)
            }
        }
    }

    private fun p(element: Element, lines: ArrayList<MDLine>) {
        val line: MDLine = getLastLine(lines)
        if (line.getContent().trim { it <= ' ' } != "") lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
        lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
        lines.add(MDLine(MDLine.MDLineType.None, 0, getTextContent(element)))
        lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
        if (line.getContent().trim { it <= ' ' } != "") lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
    }

    private fun br(lines: ArrayList<MDLine>) {
        val line: MDLine = getLastLine(lines)
        if (line.getContent().trim { it <= ' ' } != "") lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
    }

    private fun h(element: Element, lines: ArrayList<MDLine>) {
        val line: MDLine = getLastLine(lines)
        if (line.getContent().trim { it <= ' ' } != "") lines.add(MDLine(MDLine.MDLineType.None, 0, ""))

        val level: Int = element.tagName().substring(1).toInt()
        when (level) {
            1 -> lines.add(MDLine(MDLine.MDLineType.Head1, 0, getTextContent(element)))
            2 -> lines.add(MDLine(MDLine.MDLineType.Head2, 0, getTextContent(element)))
            else -> lines.add(MDLine(MDLine.MDLineType.Head3, 0, getTextContent(element)))
        }

        lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
        lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
    }

    private fun strong(element: Element, lines: ArrayList<MDLine>) {
        val line: MDLine = getLastLine(lines)
        line.append("**")
        line.append(getTextContent(element))
        line.append("**")
    }

    private fun em(element: Element, lines: ArrayList<MDLine>) {
        val line: MDLine = getLastLine(lines)
        line.append("*")
        line.append(getTextContent(element))
        line.append("*")
    }

    private fun hr(lines: ArrayList<MDLine>) {
        lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
        lines.add(MDLine(MDLine.MDLineType.HR, 0, ""))
        lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
    }

    private fun a(element: Element, lines: ArrayList<MDLine>) {
        val line: MDLine = getLastLine(lines)
        line.append("[")
        line.append(getTextContent(element))
        line.append("]")
        line.append("(")
        val url: String = element.attr("href")
        line.append(url)
        val title: String = element.attr("title")
        if (title != "") {
            line.append(" \"")
            line.append(title)
            line.append("\"")
        }
        line.append(")")
    }

    private fun img(element: Element, lines: ArrayList<MDLine>) {
        val line: MDLine = getLastLine(lines)

        line.append("![")
        val alt: String = element.attr("alt")
        line.append(alt)
        line.append("]")
        line.append("(")
        val url: String = element.attr("src")
        line.append(url)
        val title: String = element.attr("title")
        if (title != "") {
            line.append(" \"")
            line.append(title)
            line.append("\"")
        }
        line.append(")")
    }

    private fun code(element: Element, lines: ArrayList<MDLine>) {
        lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
        val line = MDLine(MDLine.MDLineType.None, 0, "    ")
        line.append(getTextContent(element).replace("\n", "    "))
        lines.add(line)
        lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
    }

    private fun ul(element: Element, lines: ArrayList<MDLine>) {
        lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
        indentation++
        orderedList = false
        val line = MDLine(MDLine.MDLineType.None, 0, "")
        line.append(getTextContent(element))
        lines.add(line)
        indentation--
        lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
    }

    private fun ol(element: Element, lines: ArrayList<MDLine>) {
        lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
        indentation++
        orderedList = true
        val line = MDLine(MDLine.MDLineType.None, 0, "")
        line.append(getTextContent(element))
        lines.add(line)
        indentation--
        lines.add(MDLine(MDLine.MDLineType.None, 0, ""))
    }

    private fun li(element: Element, lines: ArrayList<MDLine>) {
        val line: MDLine = if (orderedList) {
            MDLine(
                MDLine.MDLineType.Ordered, indentation,
                getTextContent(element)
            )
        } else {
            MDLine(
                MDLine.MDLineType.Unordered, indentation,
                getTextContent(element)
            )
        }
        lines.add(line)
    }
}
