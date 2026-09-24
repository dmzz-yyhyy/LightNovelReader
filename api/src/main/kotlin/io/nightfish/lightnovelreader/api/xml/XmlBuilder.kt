package io.nightfish.lightnovelreader.api.xml

import org.dom4j.Branch
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element

/**
 * XML文档的构建器
 * 提供 DSL 风格的 API 以构建 dom4j XML 文档
 * 一般不需要直接实例化此类，通过[xml]工厂方法使用
 *
 * @since Api 2
 */
class XmlBuilder {
    /**
     * XML元素的构建器
     * 用于在[XmlBuilder]DSL块中构建 XML 元素及其子元素
     *
     * @property element 当前正在构建的 dom4j [Element]
     *
     * @since Api 2
     */
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

        /**
         * 在当前元素下创建一个子元素
         *
         * @param name 子元素名称
         * @param attrs 子元素的属性列表
         * @param builder 用于构建子元素内容的 DSL 块
         *
         * @since Api 2
         */
        fun element(
            name: String,
            vararg attrs: Attribute = emptyArray(),
            builder: (ElementBuilder.() -> Any)? = null
        ) {
            ElementBuilder(element, name, attrs, builder)
        }

        /**
         * 创建一个 [Attribute] 键值对
         *
         * @param that 属性值
         * @return 包含当前字符串和指定值的 [Attribute]
         *
         * @since Api 2
         */
        infix fun String.to(that: Any?) = Attribute(this, that)

        /**
         * 在当前元素下创建以此字符串为名称的子元素
         *
         * @param attrs 子元素的属性列表
         * @param builder 用于构建子元素内容的 DSL 块
         *
         * @since Api 2
         */
        operator fun String.invoke(
            vararg attrs: Attribute = emptyArray(),
            builder: (ElementBuilder.() -> Any)? = null
        ) {
            ElementBuilder(element, this, attrs, builder)
        }
    }

    val document: Document = DocumentHelper
        .createDocument()
}