package io.nightfish.potatoepub.xml

import org.dom4j.Branch
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element

class XmlBuilder {
    class ElementBuilder(
        attrs: Array<out Attribute> = emptyArray(),
        val element: Element,
        builder: (ElementBuilder.() -> Any)? = null
    ) {
        constructor(
            branch: Branch,
            name: String,
            xmlns: String,
            attrs: Array<out Attribute> = emptyArray(),
            builder: (ElementBuilder.() -> Any)? = null
        ) : this(attrs, branch.addElement(name, xmlns), builder)

        constructor(
            branch: Branch,
            name: String,
            attrs: Array<out Attribute> = emptyArray(),
            builder: (ElementBuilder.() -> Any)? = null
        ) : this(attrs, branch.addElement(name), builder)

        init {
            element.apply {
                attrs
                    .filter { it.value != null }
                    .forEach { addAttribute(it.name, it.value.toString()) }
            }
            builder?.invoke(this).let {
                if (it is String && it.isNotEmpty()) {
                    element.text = it
                }
            }
        }

        fun element(
            name: String,
            vararg attrs: Attribute = emptyArray(),
            builder: (ElementBuilder.() -> Any)? = null
        ) {
            ElementBuilder(element, name, attrs, builder)
        }

        infix fun String.to(that: Any?) = Attribute(this, that)
        operator fun String.invoke(
            vararg attrs: Attribute = emptyArray(),
            builder: (ElementBuilder.() -> Any)? = null
        ) {
            ElementBuilder(element, this, attrs, builder)
        }
    }

    private val document: Document = DocumentHelper
        .createDocument()

    companion object {
        fun xml(
            root: String,
            xmlns: String,
            vararg attrs: Attribute = emptyArray(),
            builder: (ElementBuilder.() -> Any)? = null
        ): Document {
            val xmlBuilder = XmlBuilder()
            ElementBuilder(xmlBuilder.document, root, xmlns, attrs, builder)
            return xmlBuilder.document
        }

        fun xml(
            root: String,
            vararg attrs: Attribute = emptyArray(),
            builder: (ElementBuilder.() -> Any)? = null
        ): Document {
            val xmlBuilder = XmlBuilder()
            ElementBuilder(xmlBuilder.document, root, attrs, builder)
            return xmlBuilder.document
        }
    }
}