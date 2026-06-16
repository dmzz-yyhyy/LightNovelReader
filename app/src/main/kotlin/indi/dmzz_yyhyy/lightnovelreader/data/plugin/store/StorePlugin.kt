package indi.dmzz_yyhyy.lightnovelreader.data.plugin.store

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StorePlugin(
    val id: String,
    val name: String,
    val author: String,
    val summary: String,
    val description: String = "",
    @SerialName("is_nsfw") val isNsfw: Boolean = false,
    val compatibility: Compatibility = Compatibility(),
    val release: Release,
    val assets: Assets = Assets(),
    val download: Download,
    val changelog: String = ""
) {
    @Serializable
    data class Compatibility(
        @SerialName("target_api") val targetApi: Int? = null
    )

    @Serializable
    data class Release(
        @SerialName("version_name") val versionName: String,
        @SerialName("version_code") val versionCode: Int? = null
    )

    @Serializable
    data class Assets(
        val icon: Icon? = null
    ) {
        @Serializable
        data class Icon(val url: String)
    }

    @Serializable
    data class Download(
        val type: String,
        @SerialName("output_file") val outputFile: String,
        @SerialName("size_bytes") val sizeBytes: Long? = null,
        val parts: List<Part>
    ) {
        @Serializable
        data class Part(
            @SerialName("file_name") val fileName: String,
            val url: String,
            @SerialName("size_bytes") val sizeBytes: Long? = null
        )
    }
}
