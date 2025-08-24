package indi.dmzz_yyhyy.lightnovelreader.data.repository.remote

import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.RepoIndex
import indi.dmzz_yyhyy.lightnovelreader.data.repository.model.RepositoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

// Confirmed value from Shosetsu's consts
const val REPO_SOURCE_DIR: String = "/src/"

@Singleton
class RepositoryRemoteDataSource @Inject constructor() {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun downloadRepoData(repository: RepositoryEntity): RepoIndex =
        withContext(Dispatchers.IO) {
            val baseUrl = repository.url.removeSuffix("/")
            val indexFileUrl =
                "$baseUrl/index.json" // No leading slash for index.json as it's at the root of the raw path
            try {
                val response = URL(indexFileUrl).readText()
                json.decodeFromString<RepoIndex>(response)
            } catch (e: Exception) {
                throw IOException("Failed to download repository data from $indexFileUrl", e)
            }
        }

    suspend fun downloadExtension(
        repository: RepositoryEntity,
        extensionFileName: String, // e.g., "MyExtension" (without .lua)
        extensionLang: String      // e.g., "en" or "all"
    ): ByteArray = withContext(Dispatchers.IO) {
        // Ensure base URL doesn't have trailing slash for consistent concatenation
        val baseUrl = repository.url.removeSuffix("/")

        // REPO_SOURCE_DIR already has leading and trailing slashes in its definition ("/src/")
        // so we just concatenate. If it didn't, we'd need to be more careful with slashes.
        // Let's make it robust by trimming slashes from components and then joining.
        val sourceDir = REPO_SOURCE_DIR.removePrefix("/").removeSuffix("/")
        val langDir = extensionLang.removePrefix("/").removeSuffix("/")
        val file = "$extensionFileName.lua"

        val pathSegments = mutableListOf<String>()
        if (sourceDir.isNotBlank()) {
            pathSegments.add(sourceDir)
        }
        if (langDir.isNotBlank()) {
            pathSegments.add(langDir)
        }
        pathSegments.add(file)

        val extensionPath = pathSegments.joinToString("/")
        val extensionUrl = "$baseUrl/$extensionPath"

        try {
            URL(extensionUrl).readBytes()
        } catch (e: Exception) {
            throw IOException(
                "Failed to download extension $extensionFileName (lang: $extensionLang) from $extensionUrl",
                e
            )
        }
    }
}
