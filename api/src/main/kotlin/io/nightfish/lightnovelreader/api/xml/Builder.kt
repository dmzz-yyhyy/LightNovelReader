package io.nightfish.lightnovelreader.api.xml

import io.nightfish.lightnovelreader.api.xml.XmlBuilder.ElementBuilder
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element


/**
 * 创建一个带命名空间的XML文档
 *
 * @param root 根元素名称
 * @param xmlns 根元素的命名空间URI
 * @param attrs 根元素的属性列表
 * @param builder 用于构建子元素的 DSL 块
 *
 * @return 构建完成的 dom4j [Document]
 *
 * @since Api 2
 */
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

/**
 * 创建一个无命名空间的XML文档
 *
 * @param root 根元素名称
 * @param attrs 根元素的属性列表
 * @param builder 用于构建子元素的 DSL 块
 *
 * @return 构建完成的 dom4j [Document]
 *
 * @since Api 2
 */
fun xml(
    root: String,
    vararg attrs: Attribute = emptyArray(),
    builder: (ElementBuilder.() -> Any)? = null
): Document {
    val xmlBuilder = XmlBuilder()
    ElementBuilder(xmlBuilder.document, root, attrs, builder)
    return xmlBuilder.document
}

/**
 * 创建一个新的Element
 *
 * @param name 根元素名称
 * @param attrs 根元素的属性列表
 * @param builder 用于构建子元素的 DSL 块
 *
 * @return 构建完成的 dom4j [Element]
 *
 * @since Api 4
 */
fun element(
    name: String,
    vararg attrs: Attribute = emptyArray(),
    builder: (ElementBuilder.() -> Any)? = null
): Element {
    val element = DocumentHelper.createElement(name)
    return ElementBuilder(attrs, element, builder).element
}