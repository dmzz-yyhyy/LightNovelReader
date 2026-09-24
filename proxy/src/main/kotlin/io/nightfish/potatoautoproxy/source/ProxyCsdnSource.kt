package io.nightfish.potatoautoproxy.source

import io.nightfish.potatoautoproxy.Proxy
import io.nightfish.potatoautoproxy.ProxySource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

@Serializable
data class ProxyCsdnData(
    val data: ProxyCsdnDataProxyData
)


@Serializable
data class ProxyCsdnDataProxyData(
    val proxies: List<String>
)

object ProxyCsdnSource : ProxySource {
    private val defaultJson = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun getProxies(): List<Proxy> = withContext(Dispatchers.IO) {
        val json = Jsoup
            .connect("https://proxy.scdn.io/api/get_proxy.php?protocol=https&count=20")
            .ignoreContentType(true)
            .get()
            .outputSettings(
                Document.OutputSettings()
                    .prettyPrint(false)
                    .syntax(Document.OutputSettings.Syntax.xml)
            )
            .toString()
            .replace("<html><head></head><body>", "")
            .replace("</body></html>", "")
            .replace("&amp;", "&")
        val data = defaultJson.decodeFromString<ProxyCsdnData>(json)
        return@withContext data.data.proxies.map(Proxy::fromString)
    }
}