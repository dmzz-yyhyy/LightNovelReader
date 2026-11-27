package indi.dmzz_yyhyy.lightnovelreader.data.local.legacy

import kotlinx.serialization.Serializable

@Serializable
data class BookshelfEntity(

    val id: Int,
    val name: String,
    val sortType: String,
    val sortReversed: Boolean = false,
    val autoCache: Boolean,
    val systemUpdateReminder: Boolean,
    val allBookIds: List<String>,
    val pinnedBookIds: List<String>,
    val updatedBookIds: List<String>,
)
