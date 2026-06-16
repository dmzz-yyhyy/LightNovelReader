package indi.dmzz_yyhyy.lightnovelreader.ui.dialog

import indi.dmzz_yyhyy.lightnovelreader.data.plugin.store.StorePlugin

sealed interface StoreInstallState {
    data object Loading : StoreInstallState
    data class Ready(
        val plugin: StorePlugin
    ) : StoreInstallState
    data class Downloading(
        val lastPlugin: StorePlugin,
        val progress: Float
    ) : StoreInstallState
    data class Error(
        val message: String
    ) : StoreInstallState
}