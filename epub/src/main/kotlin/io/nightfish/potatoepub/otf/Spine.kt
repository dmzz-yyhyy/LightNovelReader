package io.nightfish.potatoepub.otf

import io.nightfish.potatoepub.xml.XmlBuilder

data class Spine(
    val id: String? = null,
    val itemrefList: List<Itemref>
) {
    fun element(builder: XmlBuilder.ElementBuilder) {
        builder.apply {
            "spine"("toc" to "ncx") {
                itemrefList.forEach {
                    it.element(this)
                }
            }
        }
    }

    data class Itemref(
        val id: String? = null,
        val idref: String,
        val linear: Boolean? = null,
        val properties: String? = null,
    ) {
        fun element(builder: XmlBuilder.ElementBuilder) {
            builder.apply {
                "itemref"(
                    "id" to id,
                    "idref" to idref,
                    "linear" to if (linear != null) (if (linear) "yes" else "no") else null,
                    "properties" to properties
                )
            }
        }
    }
}