package io.nightfish.potatoautoproxy

import io.nightfish.potatoautoproxy.source.Ip89Source
import io.nightfish.potatoautoproxy.source.ProxyCsdnSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.LinkedList
import java.util.Queue


object ProxyPool {
    private const val TARGET_NUMBER = 35
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val sourceList = listOf(
        ProxyCsdnSource,
        Ip89Source
    )
    private val proxyQueue: Queue<Proxy> = LinkedList()
    var enable = false

    suspend fun offerProxies(times: Int = 20): Unit = withContext(Dispatchers.IO) {
        if (proxyQueue.size < TARGET_NUMBER) {
            async {
                for (source in sourceList) {
                    try {
                        val unchecked = source.getProxies()
                        checkProxyAndJoinQueue(unchecked)
                        if (proxyQueue.size >= TARGET_NUMBER) break
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.await()
        }
        if (proxyQueue.size < TARGET_NUMBER)
            offerProxies(times - 1)
    }

    private suspend fun checkProxyAndJoinQueue(proxies: List<Proxy>): Int =
        withContext(Dispatchers.IO) {
            var number: Int
            proxies
                .filter {
                    try {
                        Jsoup
                            .connect("https://www.baidu.com")
                            .timeout(2000)
                            .proxy(it.host, it.port)
                            .ignoreSSLGet()
                        return@filter true
                    } catch (_: Exception) {
                        return@filter false
                    }
                }
                .also { number = it.size }
                .forEach(proxyQueue::offer)
            return@withContext number
        }

    fun takeProxy(): Proxy? {
        if (proxyQueue.size <= TARGET_NUMBER)
            coroutineScope.launch {
                offerProxies()
            }
        return if (proxyQueue.isNotEmpty()) proxyQueue.poll() else null
    }

    fun Connection.proxyGet(): Document {
        return takeProxy().let {
            if (it == null)
                this.get()
            else {
                this.proxy(it.host, it.port)
                this.ignoreSSLGet()
            }
        }
    }

    fun Connection.proxyPost(): Document {
        return takeProxy().let {
            if (it == null)
                this.get()
            else {
                this.proxy(it.host, it.port)
                this.ignoreSSLPost()
            }
        }
    }
}