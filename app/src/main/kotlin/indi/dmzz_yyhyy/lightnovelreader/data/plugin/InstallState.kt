package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import androidx.annotation.StringRes
import indi.dmzz_yyhyy.lightnovelreader.R

sealed interface InstallState {
    sealed class Start(
        @field:StringRes val strId: Int
    ): InstallState {
        object PrasePackageInfo: Start(R.string.plugin_install_parse_package_info)
        object PrasePluginMetadata: Start(R.string.plugin_install_parse_plugin_metadata)
        object Clean: Start(R.string.plugin_install_clean)
        object CheckPluginInstallLegality: Start(R.string.plugin_install_check_legality)
        object WritePluginMetadataToFile: Start(R.string.plugin_install_write_metadata)
        object CopyPlugin: Start(R.string.plugin_install_copy_plugin)
    }

    class Completed(
        val pluginPackage: String
    ): InstallState

    class Info(
        val name: String,
        val packageName: String,
        val versionName: String
    ): InstallState

    class Error(
        val result: Throwable
    ): InstallState
}