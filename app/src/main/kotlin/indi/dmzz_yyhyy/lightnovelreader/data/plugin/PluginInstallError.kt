package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import io.nightfish.lightnovelreader.api.ApiMetadata

sealed class PluginInstallError(message: String): Error(message) {
    class CurrentPluginVersionTooHighError: PluginInstallError("The current plugin version is higher than the plugin which was required to install")
    class AppPluginExist: PluginInstallError("The same app plugin is already")
    class PluginSignatureNotMatchError: PluginInstallError("The current plugin's signature does not match with the plugin which was required to install")
    class PluginNotSupport(val pluginUsedApiVersion: Int): PluginInstallError(
        "unsupported api version, $pluginUsedApiVersion, which the plugin used, the current api version is ${ApiMetadata.API_VERSION}"
    )
}