package indi.dmzz_yyhyy.lightnovelreader.data.json

import com.google.gson.annotations.SerializedName


data class FormattingRuleData(
    @SerializedName("book_id")
    val bookId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("is_regex")
    val isRegex: Boolean,
    @SerializedName("match")
    val match: String,
    @SerializedName("replacement")
    val replacement: String,
    @SerializedName("is_enabled")
    val isEnabled: Boolean
)
