package indi.dmzz_yyhyy.lightnovelreader.data.repository.model

import kotlinx.serialization.Serializable

@Serializable
data class RepoIndex(
    val name: String,
    val website: String,
    val support: String,
    val extensions: List<RepoExtension>
)

@Serializable
data class RepoExtension(
    val id: Int,
    val name: String,
    val fileName: String,
    val imageURL: String,
    val lang: String,
    val version: String,
    val md5: String,
    val type: String,
    val description: String = ""
)
