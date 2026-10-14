package io.nightfish.lightnovelreader.ktor.browser

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.Url

/** Adds configurable headers for a top-level browser page navigation. */
class BrowserRequestConfig {
    /** When set, these are the headers observed on the WebView's main-frame request. */
    var headersProvider: ((Url) -> Map<String, String>)? = null
    var cookieProvider: ((Url) -> String?)? = null
    var userAgent: String =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/154.0.0.0 Safari/537.36"

    var accept: String =
        "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"
    var acceptLanguage: String = "zh-CN,zh;q=0.9"
    var cacheControl: String = "max-age=0"
    var secChUa: String = "\"Chromium\";v=\"154\", \"Google Chrome\";v=\"154\", \"Not A(Brand\";v=\"99\""
    var secChUaMobile: String = "?0"
    var secChUaPlatform: String = "\"macOS\""
    var secFetchDest: String = "document"
    var secFetchMode: String = "navigate"
    var secFetchSite: String? = null
    var secFetchUser: String = "?1"
    var priority: String = "u=0, i"
    var upgradeInsecureRequests: String = "1"
}

/**
 * A Ktor client plugin for requests that should resemble a Chrome page navigation.
 * Install it on the client used for Wenku8 page requests.
 */
val BrowserRequest = createClientPlugin("BrowserRequest", ::BrowserRequestConfig) {
    val config = pluginConfig

    onRequest { request, _ ->
        val observedHeaders = config.headersProvider?.invoke(request.url.build()).orEmpty()
        if (observedHeaders.isNotEmpty()) {
            observedHeaders.forEach { (name, value) ->
                if (!name.equals(HttpHeaders.Cookie, ignoreCase = true) &&
                    !name.equals(HttpHeaders.Host, ignoreCase = true) &&
                    !name.equals(HttpHeaders.Connection, ignoreCase = true) &&
                    !name.startsWith(":")) {
                    request.headers[name] = value
                }
            }
            config.cookieProvider?.invoke(request.url.build())?.let {
                request.headers[HttpHeaders.Cookie] = it
            }
            return@onRequest
        }
        request.headers.setIfMissing(HttpHeaders.UserAgent, config.userAgent)
        request.headers.setIfMissing(HttpHeaders.Accept, config.accept)
        request.headers.setIfMissing(HttpHeaders.AcceptLanguage, config.acceptLanguage)
        request.headers.setIfMissing(HttpHeaders.CacheControl, config.cacheControl)
        request.headers.setIfMissing("Sec-CH-UA", config.secChUa)
        request.headers.setIfMissing("Sec-CH-UA-Mobile", config.secChUaMobile)
        request.headers.setIfMissing("Sec-CH-UA-Platform", config.secChUaPlatform)
        request.headers.setIfMissing("Sec-Fetch-Dest", config.secFetchDest)
        request.headers.setIfMissing("Sec-Fetch-Mode", config.secFetchMode)
        request.headers.setIfMissing("Sec-Fetch-Site", config.secFetchSite ?: request.fetchSite())
        request.headers.setIfMissing("Sec-Fetch-User", config.secFetchUser)
        request.headers.setIfMissing("Priority", config.priority)
        request.headers.setIfMissing("Upgrade-Insecure-Requests", config.upgradeInsecureRequests)
        config.cookieProvider?.invoke(request.url.build())?.let {
            request.headers[HttpHeaders.Cookie] = it
        }
    }
}

private fun HttpRequestBuilder.fetchSite(): String {
    val referer = headers[HttpHeaders.Referrer] ?: return "none"
    val source = runCatching { Url(referer) }.getOrNull() ?: return "cross-site"
    val sameOrigin = source.protocol == url.protocol &&
            source.host.equals(url.host, ignoreCase = true) &&
            source.port == url.port
    return if (sameOrigin) "same-origin" else "cross-site"
}

private fun io.ktor.http.HeadersBuilder.setIfMissing(name: String, value: String) {
    if (get(name) == null) append(name, value)
}
