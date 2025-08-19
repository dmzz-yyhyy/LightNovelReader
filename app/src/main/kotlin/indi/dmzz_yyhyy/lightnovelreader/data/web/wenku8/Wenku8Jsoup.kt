package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8

import android.util.Log
import indi.dmzz_yyhyy.lightnovelreader.utils.UserAgentGenerator
import indi.dmzz_yyhyy.lightnovelreader.utils.autoReconnectionPost
import indi.dmzz_yyhyy.lightnovelreader.utils.update
import kotlinx.coroutines.delay
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.Instant
import kotlin.io.encoding.Base64
import kotlin.random.Random

private var requestCount = 0

fun Connection.wenku8Cookie(): Connection =
    this
        .userAgent(UserAgentGenerator.generate())
        .cookie(" Hm_lvt_acfbfe93830e0272a88e1cc73d4d6d0f", "1737964211")
        .cookie(" PHPSESSID", "261c62b5dae26868bba643433e859ce6")
        .cookie(
            " jieqiUserInfo",
            "jieqiUserId%3D1125456%2CjieqiUserName%3Dyyhyy%2CjieqiUserGroup%3D3%2CjieqiUserVip%3D0%2CjieqiUserPassword%3Deb62861281462fd923fb99218735fef0%2CjieqiUserName_un%3Dyyhyy%2CjieqiUserHonor_un%3D%26%23x4E2D%3B%26%23x7EA7%3B%26%23x4F1A%3B%26%23x5458%3B%2CjieqiUserGroupName_un%3D%26%23x666E%3B%26%23x901A%3B%26%23x4F1A%3B%26%23x5458%3B%2CjieqiUserLogin%3D1739294499"
        )
        .cookie(" jieqiVisitInfo", "jieqiUserLogin%3D1739294499%2CjieqiUserId%3D1125456")
        .cookie(
            " cf_clearance",
            "3zr0PrHC91IKoMSddax50XdS4Z_w10P.MHnUWfhwvuE-1739294164-1.2.1.1-KudGwf7eifsQWo9tIfX7Gg9Z_VwgSDRHr2erMBcjfHcOJqyg6zpM.XQYS54P0zx8bgSOrmvyRU5xcR9EuCA9aiNSec_tY.r82Lq6w3O_EEPgZuG1HdqjGCgMH11Mud34v5h3lMSGG3PBLCdXD5GXqDE1mPWDzIWyDbprUKg_YZ09DekRXkpyKwa.rt6Pz8LmBN5aVAkoF06sdPcLoUHqnyKe2584pWQ8nWrsM7frhohd8oAH0u12GPD_z8k_SHhflswjC7...cUz.5Hxonur_829PrCsjt.vJqAal0eqE5AmfBJ3FLWO1I3c0vKsVkSO3rrA8bH0v0yDHfatKKO3ww"
        )
        .cookie(" HMACCOUNT", "E7837B0FF79F0590")
        .cookie(" Hm_lvt_d72896ddbf8d27c750e3b365ea2fc902", "1739294365,1739294389,1739294442,1739294467")
        .cookie(" Hm_lpvt_d72896ddbf8d27c750e3b365ea2fc902", "1739294503")

suspend fun wenku8Api(request: String): Document? {
    while (requestCount > 3) {
        delay(500)
    }
    requestCount++
    Log.i("Wenku8API", "require to wenku8 with $request")
    delay(Random.Default.nextLong(500, 800))
    return Jsoup
        .connect(update("eNpb85aBtYRBMaOkpMBKXz-xoECvPDUvu9RCLzk_Vz8xL6UoPzNFryCjAAAfiA5Q").toString())
        .data(
            "request", Base64.encode(request.toByteArray()),
            "timetoken", Instant.now().toEpochMilli().toString(),
            "appver", "1.21"
        )
        .autoReconnectionPost()
        ?.outputSettings(
            Document.OutputSettings()
                .prettyPrint(false)
                .syntax(Document.OutputSettings.Syntax.xml)
        ).also {
            if (it == null) delay(10000)
            requestCount--
        }
}