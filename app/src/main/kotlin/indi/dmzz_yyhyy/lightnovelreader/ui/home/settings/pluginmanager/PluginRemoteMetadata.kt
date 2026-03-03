package indi.dmzz_yyhyy.lightnovelreader.ui.home.settings.pluginmanager

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemotePluginMetadata(
    val id: String,
    val version: Int,
    val name: String,
    @SerialName("version_name")
    val versionName: String,
    val author: String,
    val description: String,
    @SerialName("compressed_file_number")
    val compressedFileNumber: Int
)