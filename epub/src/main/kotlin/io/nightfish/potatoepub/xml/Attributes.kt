package io.nightfish.potatoepub.xml

class Version(version: String) : Attribute("version", version)
sealed class TextDirection(textDirection: String) : Attribute("dir", textDirection) {
    data object LTR : TextDirection("ltr")
    data object RTL : TextDirection("rtl")
    data object AUTO : TextDirection("auto")
}

class XmlLang(local: String) : Attribute("xml:lang", local)