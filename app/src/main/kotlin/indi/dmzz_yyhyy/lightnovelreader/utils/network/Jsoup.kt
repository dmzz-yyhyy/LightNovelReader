package indi.dmzz_yyhyy.lightnovelreader.utils.network

import android.util.Log
import io.nightfish.potatoautoproxy.ProxyPool
import kotlinx.coroutines.delay
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.SocketException
import javax.net.ssl.SSLHandshakeException

@Deprecated("use CxHttp to get fast request")
suspend fun Connection.autoReconnectionGet(reconnectTime: Int = 5, reconnectDelay: Long = 250, isUesProxy: Boolean = false): Document? =
    autoReconnection(
        reconnectTime,
        reconnectDelay,
        isUesProxy,
        { this.get() },
        { this@autoReconnectionGet.proxyGet() }
    )

@Deprecated("use CxHttp to get fast request")
suspend fun Connection.autoReconnectionPost(reconnectTime: Int = 5, reconnectDelay: Long = 250, isUesProxy: Boolean = false): Document? =
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
        return retry(reconnectTime, reconnectDelay) {
            autoReconnection(reconnectTime - 1, (reconnectDelay * 2), isUesProxy, block, block2)
        }
    } catch (e: SocketException) {
        Log.e("Network", "failed to get data from ${e.cause}, last reconnection times: $reconnectTime")
        e.printStackTrace()
        return retry(reconnectTime, reconnectDelay) {
            autoReconnection(reconnectTime - 1, (reconnectDelay * 2), isUesProxy, block, block2)
        }
    } catch (e: SSLHandshakeException) {
        Log.e("Network", "failed to get data, last reconnection times: $reconnectTime")
        e.printStackTrace()
        return retry(reconnectTime, reconnectDelay) {
            autoReconnection(reconnectTime - 1, (reconnectDelay * 2), isUesProxy, block, block2)
        }
    } catch (e: IOException) {
        Log.e("Network", "failed to get data, last reconnection times:  $reconnectTime")
        e.printStackTrace()
        return retry(reconnectTime, reconnectDelay) {
            autoReconnection(reconnectTime - 1, (reconnectDelay * 2), isUesProxy, block, block2)
        }
    }
}

private suspend fun retry(reconnectTimes: Int, reconnectDelay: Long, block: suspend () -> Document?): Document? {
    if (reconnectTimes < 1) return null
    delay(reconnectDelay)
    return block.invoke()
}

fun Document.selectFirstXpath(path: String) =
    this.selectXpath(path).firstOrNull()