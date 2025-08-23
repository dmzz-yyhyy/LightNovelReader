package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.exploration.model

import indi.dmzz_yyhyy.lightnovelreader.data.local.room.entity.BookInformationEntity

data class ExtensionExplorationUI(
    val books: List<BookInformationEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedExtensionId: String? = null
)

data class ExtensionExplorationHomeUI(
    val availableExtensions: List<ExtensionInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ExtensionInfo(
    val id: String,
    val name: String,
    val description: String,
    val isEnabled: Boolean = true
)
