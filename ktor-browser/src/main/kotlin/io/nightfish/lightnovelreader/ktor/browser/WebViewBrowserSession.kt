package io.nightfish.lightnovelreader.ktor.browser

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

/** Runs an unattached WebView to establish the same browser session used by Ktor. */
class WebViewBrowserSession(
    context: Context,
    private val initialCookies: (String) -> Map<String, String> = { emptyMap() },
) {
    private val appContext = context.applicationContext
    private val mutex = Mutex()
    private val sessions = ConcurrentHashMap<String, BrowserSession>()

    fun headersFor(url: Url): Map<String, String> = sessions[url.host]?.headers.orEmpty()

    fun cookieHeaderFor(url: Url): String? {
        val cookies = linkedMapOf<String, String>()
        initialCookies(url.host).forEach { (name, value) ->
            cookies[name] = URLEncoder.encode(value, Charsets.UTF_8.name())
        }
        sessions[url.host]?.cookies?.split(';')?.forEach { part ->
            val pair = part.trim().split('=', limit = 2)
            if (pair.size == 2) cookies[pair[0]] = pair[1]
        }
        return cookies.entries.takeIf { it.isNotEmpty() }
            ?.joinToString("; ") { (name, value) -> "$name=$value" }
    }

    suspend fun establish(url: String, force: Boolean = false): BrowserSession = mutex.withLock {
        val host = Url(url).host
        if (!force) sessions[host]?.let { return@withLock it }
        val session = withTimeout(35.seconds) {
            withContext(Dispatchers.Main.immediate) { loadPage(url, host) }
        }
        sessions[host] = session
        session
    }

    @SuppressLint("SetJavaScriptEnabled")
    private suspend fun loadPage(url: String, host: String): BrowserSession =
        suspendCancellableCoroutine { continuation ->
            val handler = Handler(Looper.getMainLooper())
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            val webView = WebView(appContext)
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
            webView.settings.allowFileAccess = false
            webView.settings.allowContentAccess = false

            // These values are passed to both the browser navigation and the Ktor request.
            val navigationHeaders = linkedMapOf(
                "User-Agent" to webView.settings.userAgentString,
                "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
                "Accept-Language" to "zh-CN,zh;q=0.9",
                "Accept-Encoding" to "gzip, deflate, br",
                "Cache-Control" to "max-age=0",
                "Sec-Fetch-Dest" to "document",
                "Sec-Fetch-Mode" to "navigate",
                "Sec-Fetch-Site" to "none",
                "Sec-Fetch-User" to "?1",
                "Upgrade-Insecure-Requests" to "1",
            )
            val mainFrameHeaders = AtomicReference<Map<String, String>>(emptyMap())
            var mainFrameStatus = 0
            var finished = false

            fun cleanup() {
                if (finished) return
                finished = true
                handler.removeCallbacksAndMessages(null)
                webView.stopLoading()
                webView.destroy()
            }

            fun complete() {
                if (!continuation.isActive) return
                val captured = mainFrameHeaders.get()
                    .filterKeys { key ->
                        !key.equals("Cookie", true) &&
                            !key.equals("Host", true) &&
                            !key.equals("Connection", true) &&
                            !key.equals("Accept-Encoding", true) &&
                            !key.startsWith(":")
                    }
                val headers = navigationHeaders.toMutableMap().apply { putAll(captured) }
                val cookies = cookieManager.getCookie(url).orEmpty()
                cookieManager.flush()
                cleanup()
                continuation.resume(BrowserSession(headers, cookies))
            }

            fun checkPage() {
                if (!continuation.isActive || finished) return
                webView.evaluateJavascript(
                    """(function() {
                        var challenged = !!document.querySelector(
                            '#challenge-form, #cf-challenge-running, .cf-turnstile, [id^="cf-chl-"]'
                        ) || /just a moment|checking your browser|稍等片刻/i.test(document.title);
                        return (challenged ? 'CHALLENGE' : 'PAGE') + '\n' +
                            document.title + '\n' + document.documentElement.outerHTML.slice(0, 512);
                    })()""".trimIndent()
                ) { json ->
                    if (!continuation.isActive || finished) return@evaluateJavascript
                    val page = runCatching { JSONArray("[$json]").getString(0) }.getOrDefault("")
                    if (mainFrameStatus in 200..299 && page.startsWith("PAGE\n") &&
                        page.contains("<html", true)) {
                        complete()
                    } else {
                        handler.postDelayed({ checkPage() }, 500)
                    }
                }
            }

            webView.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView, pageUrl: String, favicon: Bitmap?) {
                    mainFrameStatus = 0
                }

                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest,
                ): WebResourceResponse? {
                    if (request.isForMainFrame && request.url.host == host) {
                        mainFrameHeaders.set(request.requestHeaders.toMap())
                    }
                    return null
                }

                override fun onReceivedHttpError(
                    view: WebView,
                    request: WebResourceRequest,
                    errorResponse: WebResourceResponse,
                ) {
                    if (request.isForMainFrame) mainFrameStatus = errorResponse.statusCode
                }

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: WebResourceError,
                ) {
                    if (request.isForMainFrame) mainFrameStatus = -1
                }

                override fun onPageFinished(view: WebView, pageUrl: String) {
                    if (pageUrl.startsWith("https://$host/")) {
                        if (mainFrameStatus == 0) mainFrameStatus = 200
                        checkPage()
                    }
                }
            }

            continuation.invokeOnCancellation { handler.post { cleanup() } }
            val cookieEntries = initialCookies(host).entries.toList()
            fun seedCookie(index: Int) {
                if (!continuation.isActive) return
                if (index == cookieEntries.size) {
                    webView.loadUrl(url, navigationHeaders.filterKeys { it != "User-Agent" })
                    return
                }
                val (name, value) = cookieEntries[index]
                val encoded = URLEncoder.encode(value, Charsets.UTF_8.name())
                cookieManager.setCookie(url, "$name=$encoded; Domain=$host; Path=/; Secure") {
                    seedCookie(index + 1)
                }
            }
            seedCookie(0)
        }
}

data class BrowserSession(val headers: Map<String, String>, val cookies: String)
