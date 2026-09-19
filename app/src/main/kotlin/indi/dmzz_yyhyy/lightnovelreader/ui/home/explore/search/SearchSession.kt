package indi.dmzz_yyhyy.lightnovelreader.ui.home.explore.search

import androidx.lifecycle.SavedStateHandle

internal data class SearchRequest(
    val typeId: String,
    val keyword: String,
    val keepSingleResult: Boolean = false
)

/** Saves the editable input separately from the query that produced the displayed results. */
internal class SearchSession(private val savedState: SavedStateHandle) {
    var keyword: String
        get() = savedState["search.keyword"] ?: ""
        set(value) { savedState["search.keyword"] = value }
    var selectedType: String?
        get() = savedState["search.type"]
        set(value) { savedState["search.type"] = value }

    val submitted: SearchRequest?
        get() {
            val type = savedState.get<String>("search.submittedType") ?: return null
            val keyword = savedState.get<String>("search.submittedKeyword") ?: return null
            return SearchRequest(type, keyword, savedState["search.keepSingleResult"] ?: false)
        }

    fun initialize(author: String?, authorType: String?, defaultType: String?): SearchRequest? {
        if (savedState.get<Boolean>("search.initialized") == true) return submitted
        savedState["search.initialized"] = true
        keyword = author.orEmpty().trim()
        selectedType = if (author == null) defaultType else authorType
        return if (keyword.isNotBlank() && authorType != null && author != null) {
            SearchRequest(authorType, keyword, keepSingleResult = true)
        } else null
    }

    fun submit(request: SearchRequest) {
        savedState["search.submittedType"] = request.typeId
        savedState["search.submittedKeyword"] = request.keyword
        savedState["search.keepSingleResult"] = request.keepSingleResult
    }
}
