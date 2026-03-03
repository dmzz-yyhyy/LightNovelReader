package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8

import cxhttp.CxHttp
import cxhttp.response.Response
import indi.dmzz_yyhyy.lightnovelreader.utils.network.UserAgentGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.nio.charset.Charset

fun wenku8Cookies(): Map<String, String> = mapOf(
    "Hm_lvt_acfbfe93830e0272a88e1cc73d4d6d0f" to "1737964211",
    "PHPSESSID" to "261c62b5dae26868bba643433e859ce6",
    "jieqiUserInfo" to "jieqiUserId%3D1125456%2CjieqiUserName%3Dyyhyy%2CjieqiUserGroup%3D3%2CjieqiUserVip%3D0%2CjieqiUserPassword%3Deb62861281462fd923fb99218735fef0%2CjieqiUserName_un%3Dyyhyy%2CjieqiUserHonor_un%3D%26%23x4E2D%3B%26%23x7EA7%3B%26%23x4F1A%3B%26%23x5458%3B%2CjieqiUserGroupName_un%3D%26%23x666E%3B%26%23x901A%3B%26%23x4F1A%3B%26%23x5458%3B%2CjieqiUserLogin%3D1739294499",
    "jieqiVisitInfo" to "jieqiUserLogin%3D1739294499%2CjieqiUserId%3D1125456",
    "cf_clearance" to "3zr0PrHC91IKoMSddax50XdS4Z_w10P.MHnUWfhwvuE-1739294164-1.2.1.1-KudGwf7eifsQWo9tIfX7Gg9Z_VwgSDRHr2erMBcjfHcOJqyg6zpM.XQYS54P0zx8bgSOrmvyRU5xcR9EuCA9aiNSec_tY.r82Lq6w3O_EEPgZuG1HdqjGCgMH11Mud34v5h3lMSGG3PBLCdXD5GXqDE1mPWDzIWyDbprUKg_YZ09DekRXkpyKwa.rt6Pz8LmBN5aVAkoF06sdPcLoUHqnyKe2584pWQ8nWrsM7frhohd8oAH0u12GPD_z8k_SHhflswjC7...cUz.5Hxonur_829PrCsjt.vJqAal0eqE5AmfBJ3FLWO1I3c0vKsVkSO3rrA8bH0v0yDHfatKKO3ww",
    "HMACCOUNT" to "E7837B0FF79F0590",
    "Hm_lvt_d72896ddbf8d27c750e3b365ea2fc902" to "1739294365,1739294389,1739294442,1739294467",
    "Hm_lpvt_d72896ddbf8d27c750e3b365ea2fc902" to "1739294503"
)


private val requestLimiter = Semaphore(3)

suspend fun autoReconnectionGetWithWenku8Cookie(url: String): Document? = withContext(Dispatchers.IO) {
    requestLimiter.withPermit {
        suspend fun get(): Response {
            return CxHttp
                .get(url){
                    header("user-agent", UserAgentGenerator.generate())
                    header("cookie",wenku8Cookies().map { "${it.key}=${it.value}" }.joinToString(separator = ";"))
                }
                .scope(this)
                .await()
        }
        var retryTime = 3
        var retryDelay = 2500L
        var response = get()
        while (!response.isSuccessful && retryTime >= 1) {
            response = get()
            retryTime--
            delay(retryDelay)
            retryDelay *= 2
        }
        val doc = response.body
            ?.bytes()
            ?.toString(charset = Charset.forName("GBK"))
            ?.let(Jsoup::parse)
            ?.outputSettings(
                Document.OutputSettings()
                    .prettyPrint(false)
                    .syntax(Document.OutputSettings.Syntax.xml)
            )
        return@withContext doc
    }
}
