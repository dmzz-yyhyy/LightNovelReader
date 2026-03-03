package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import io.nightfish.lightnovelreader.api.plugin.Plugin
import kotlinx.serialization.Serializable

@Serializable
data class PluginMetadata(
    val packageName: String,
    val name: String,
    val version: Int,
    val versionName: String,
    val author: String,
    val description: String,
    val updateUrl: String,
    val apiVersion: Int,
    val hasSignature: Boolean,
    val source: PluginSource = PluginSource.LocalPackage
) {
    companion object {
        fun parse(plugin: Plugin, packageName: String, hasSignature: Boolean) =
            PluginMetadata(
                packageName = packageName,
                name = plugin.name,
                version = plugin.version,
                versionName = plugin.versionName,
                author = plugin.author,
                description = plugin.description,
                updateUrl = plugin.updateUrl,
                apiVersion = plugin.apiVersion,
                hasSignature = hasSignature
            )
    }
}