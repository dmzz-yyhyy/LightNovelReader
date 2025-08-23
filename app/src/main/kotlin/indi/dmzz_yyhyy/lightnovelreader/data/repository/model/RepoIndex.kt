package indi.dmzz_yyhyy.lightnovelreader.data.repository.model

import kotlinx.serialization.Serializable

@Serializable
data class RepoIndex(
    val authors: List<RepoAuthor> = emptyList(),
    val libraries: List<RepoLibrary> = emptyList(),
    val scripts: List<RepoExtension> = emptyList(),
    val styles: List<RepoStyle> = emptyList(),
    val js: List<RepoJS> = emptyList()
)

@Serializable
data class RepoAuthor(
    val id: Int,
    val name: String,
    val desc: String,
    val imageURL: String,
    val website: String
)

@Serializable
data class RepoLibrary(
    val name: String,
    val ver: String,
    val url: String?,
    val hash: String
)

@Serializable
data class RepoExtension(
    val id: Int,
    val name: String,
    val fileName: String,
    val imageURL: String,
    val lang: String,
    val ver: String, // Note: Shosetsu uses "ver" instead of "version"
    val libVer: String,
    val md5: String,
    val type: String
)

@Serializable
data class RepoStyle(
    val name: String,
    val ver: String,
    val url: String?,
    val hash: String
)

@Serializable
data class RepoJS(
    val name: String,
    val ver: String,
    val url: String?,
    val hash: String
)
