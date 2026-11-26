package indi.dmzz_yyhyy.lightnovelreader.data.local.legacy

import kotlinx.serialization.Serializable

@Serializable
data class FormattingRuleEntity(

    val id: Int? = null,
    val bookId: String,
    val name: String,
    val isRegex: Boolean,
    val match: String,
    val replacement: String,
    val isEnabled: Boolean
)
