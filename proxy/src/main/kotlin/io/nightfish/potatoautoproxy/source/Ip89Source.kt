package io.nightfish.potatoautoproxy.source

import io.nightfish.potatoautoproxy.Proxy
import io.nightfish.potatoautoproxy.ProxySource
import io.nightfish.potatoautoproxy.ignoreSSLGet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

object Ip89Source : ProxySource {
    override suspend fun getProxies(): List<Proxy> = withContext(Dispatchers.IO) {
        return@withContext Jsoup
            .connect("https://api.89ip.cn/tqdl.html?api=1&num=40&port=&address=&isp=")
            .ignoreContentType(true)
            .get()
            .body()
            .text()
            .replace("更好用的代理ip请访问：www.qiyunip.com", "")
            .split(" ")
            .filter(String::isNotBlank)
            .map(Proxy::fromString)
    }
}

fun main() {
    Jsoup
        .connect("https://proxydb.net/?country=CN")
        .ignoreContentType(true)
        .get()
        .body()
        .select("body > div > div.table-responsive > table > tbody > tr")
        .mapNotNull {
            Proxy(
                host = it.selectFirst("td:nth-child(1) > a")?.text() ?: return@mapNotNull null,
                port = it.selectFirst("td:nth-child(2) > div")?.text()?.toInt()
                    ?: return@mapNotNull null
            )
        }
        .forEach {
            try {
                Jsoup
                    .connect("https://www.baidu.com")
                    .timeout(4000)
                    .proxy(it.host, it.port)
                    .ignoreSSLGet()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}