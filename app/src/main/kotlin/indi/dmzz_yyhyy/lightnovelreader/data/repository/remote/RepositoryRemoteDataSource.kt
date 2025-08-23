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

@Singleton
class RepositoryRemoteDataSource @Inject constructor() {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun downloadRepoData(repository: RepositoryEntity): RepoIndex = withContext(Dispatchers.IO) {
        try {
            val response = URL(repository.url).readText()
            json.decodeFromString<RepoIndex>(response)
        } catch (e: Exception) {
            throw IOException("Failed to download repository data from ${repository.url}", e)
        }
    }

    suspend fun downloadExtension(
        repository: RepositoryEntity,
        fileName: String
    ): ByteArray = withContext(Dispatchers.IO) {
        try {
            val extensionUrl = "${repository.url.removeSuffix("/")}/$fileName"
            URL(extensionUrl).readBytes()
        } catch (e: Exception) {
            throw IOException("Failed to download extension $fileName from ${repository.url}", e)
        }
    }
}
