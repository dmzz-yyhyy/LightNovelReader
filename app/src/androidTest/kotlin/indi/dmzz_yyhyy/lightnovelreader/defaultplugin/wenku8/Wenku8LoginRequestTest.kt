package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.michaelbull.result.getOrElse
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.nightfish.lightnovelreader.ktor.browser.BrowserRequest
import io.nightfish.lightnovelreader.ktor.browser.WebViewBrowserSession
import io.nightfish.lightnovelreader.ktor.browser.browserResponseDecoders
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.charset.Charset
import kotlin.time.Duration.Companion.seconds

/** Live integration test for the production Ktor client and Wenku8 login page. */
@RunWith(AndroidJUnit4::class)
class Wenku8LoginRequestTest {
    @Test
    fun productionClientReturnsLoginPage() = runBlocking {
        val api = Wenku8Api(ApplicationProvider.getApplicationContext())
        try {
            val page = withTimeout(60.seconds) {
                api.getWithWenku8Cookie(LOGIN_URL).getOrElse { throw it }
            }
            assertTrue("Expected Wenku8 login HTML, title=${page.title()}",
                page.title().isNotBlank() &&
                    !page.title().contains("Just a moment", ignoreCase = true) &&
                    page.selectFirst("html") != null)
        } finally {
            api.ktorClient.close()
        }
    }

    @Test
    fun hiddenWebViewSessionIsUsedByKtor() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val session = WebViewBrowserSession(context)
        val browser = withTimeout(45.seconds) { session.establish(LOGIN_URL) }
        assertTrue(browser.headers["User-Agent"].orEmpty().contains("Chrome/"))
        assertEquals(browser.headers, session.headersFor(io.ktor.http.Url(LOGIN_URL)))
        if (browser.cookies.contains("cf_clearance=")) {
            assertTrue(session.cookieHeaderFor(io.ktor.http.Url(LOGIN_URL)).orEmpty()
                .contains("cf_clearance="))
        }
        val client = HttpClient(OkHttp) {
            install(BrowserRequest) {
                headersProvider = { url -> session.headersFor(url) }
                cookieProvider = { url -> session.cookieHeaderFor(url) }
            }
            install(ContentEncoding) {
                gzip()
                deflate()
                browserResponseDecoders()
            }
        }
        try {
            assertLoginPage(client)
        } finally {
            client.close()
        }
    }

    private suspend fun assertLoginPage(client: HttpClient) {
        withTimeout(45.seconds) {
            val response = client.get(LOGIN_URL) {
                timeout {
                    requestTimeoutMillis = 35_000
                    connectTimeoutMillis = 15_000
                    socketTimeoutMillis = 15_000
                }
            }
            val html = response.bodyAsText(Charset.forName("GBK"))
            val title = Jsoup.parse(html).title().take(120)
            val responseInfo = listOf("Server", "Via", "Content-Type", "Content-Encoding", "Location")
                .mapNotNull { name -> response.headers[name]?.let { "$name=$it" } }
                .joinToString(", ")
            val isCloudflareChallenge = title.contains("just a moment", ignoreCase = true) ||
                    title.contains("checking your browser", ignoreCase = true) ||
                    html.contains("id=\"challenge-form\"", ignoreCase = true) ||
                    html.contains("id='challenge-form'", ignoreCase = true) ||
                    html.contains("cf-chl-", ignoreCase = true)

            assertTrue(
                "Expected a successful HTML response; status=${response.status.value}, " +
                        "headers=[$responseInfo], title=[$title], cloudflareChallenge=$isCloudflareChallenge",
                response.status.isSuccess() &&
                        html.contains("<html", ignoreCase = true) &&
                        !isCloudflareChallenge
            )
        }
    }

    private companion object {
        const val LOGIN_URL = "https://www.wenku8.net/login.php"
    }
}
