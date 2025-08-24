package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.browse.model

import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.ExtensionEntity

data class BrowseUI(
    val extensions: List<BrowseExtensionItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val showSearch: Boolean = false
)

data class BrowseExtensionItem(
    val extension: ExtensionEntity,
    val isInstalled: Boolean = false,
    val installedVersion: String? = null,
    val isUpdateAvailable: Boolean = false,
    val updateVersion: String? = null,
    val isInstalling: Boolean = false
)
