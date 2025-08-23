package indi.dmzz_yyhyy.lightnovelreader.ui.extensions.installed.model

import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.InstalledExtensionEntity

data class InstalledUI(
    val extensions: List<InstalledExtensionItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class InstalledExtensionItem(
    val extension: InstalledExtensionEntity,
    val isUninstalling: Boolean = false
)
