package indi.dmzz_yyhyy.lightnovelreader.utils

import android.util.Log
import io.nightfish.potatoautoproxy.ProxyPool
import kotlinx.coroutines.delay
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.SocketException
import javax.net.ssl.SSLHandshakeException

suspend fun Connection.autoReconnectionGet(reconnectTime: Int = 3, reconnectDelay: Long = 250, isUesProxy: Boolean = false): Document? =
    autoReconnection(
        reconnectTime,
        reconnectDelay,
        isUesProxy,
        { this.get() },
        { this@autoReconnectionGet.proxyGet() }
    )

suspend fun Connection.autoReconnectionPost(reconnectTime: Int = 3, reconnectDelay: Long = 250, isUesProxy: Boolean = false): Document? =
    autoReconnection(
        reconnectTime,
        reconnectDelay,
        isUesProxy,
        { this.post() },
        { this@autoReconnectionPost.proxyPost() }
    )

private suspend fun autoReconnection(
    reconnectTime: Int = 3,
    reconnectDelay: Long = 250,
    isUesProxy: Boolean = false,
    block: suspend () -> Document?,
    block2: suspend ProxyPool.() -> Document?
): Document? {
    try {
        if (ProxyPool.enable && isUesProxy)
            ProxyPool.apply {
                return block2.invoke(this)
            }
        else return block.invoke()
    } catch (e: HttpStatusException) {
        Log.e("Network", "failed to get data from ${e.url}, last reconnection times: $reconnectTime")
        e.printStackTrace()
        retry(reconnectTime, reconnectDelay) {
            autoReconnection(reconnectTime - 1, reconnectDelay, isUesProxy, block, block2)
        }
    } catch (e: SocketException) {
        Log.e("Network", "failed to get data, last reconnection times: $reconnectTime")
        e.printStackTrace()
        retry(reconnectTime, reconnectDelay) {
            autoReconnection(reconnectTime - 1, reconnectDelay, isUesProxy, block, block2)
        }
    } catch (e: SSLHandshakeException) {
        Log.e("Network", "failed to get data, last reconnection times: $reconnectTime")
        e.printStackTrace()
        retry(reconnectTime, reconnectDelay) {
            autoReconnection(reconnectTime - 1, reconnectDelay, isUesProxy, block, block2)
        }
    } catch (e: IOException) {
        Log.e("Network", "failed to get data, last reconnection times:  $reconnectTime")
        e.printStackTrace()
        retry(reconnectTime, reconnectDelay) {
            autoReconnection(reconnectTime - 1, reconnectDelay, isUesProxy, block, block2)
        }
    }
    return null
}

private suspend fun retry(reconnectTimes: Int, reconnectDelay: Long, block: suspend () -> Document?): Document? {
    if (reconnectTimes < 1) return null
    delay(reconnectDelay)
    return block.invoke()
}

suspend fun Connection.autoReconnectionGetJsonText(): String? =
    this.ignoreContentType(true)
        .autoReconnectionGet()
        ?.outputSettings(
            Document.OutputSettings()
                .prettyPrint(false)
                .syntax(Document.OutputSettings.Syntax.xml)
        )
        ?.body()
        ?.text()