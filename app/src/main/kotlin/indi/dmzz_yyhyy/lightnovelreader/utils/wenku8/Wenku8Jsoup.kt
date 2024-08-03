package indi.dmzz_yyhyy.lightnovelreader.utils.wenku8

import indi.dmzz_yyhyy.lightnovelreader.utils.update
import java.time.Instant
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document


fun Connection.wenku8Cookie(): Connection =
    this.cookie("__51uvsct__1xtyjOqSZ75DRXC0", "1")
        .cookie(" __51vcke__1xtyjOqSZ75DRXC0", "5fd1e310-a176-5ee6-9144-ed977bccf14e")
        .cookie(" __51vuft__1xtyjOqSZ75DRXC0", "1691164424380")
        .cookie(
            " Hm_lvt_d72896ddbf8d27c750e3b365ea2fc902",
            "1695572903,1695666346,1696009387,1696966471"
        )
        .cookie(" Hm_lvt_acfbfe93830e0272a88e1cc73d4d6d0f", "1721130033,1721491724,1721570341")
        .cookie(" PHPSESSID", "4d1c461c284bfa784985dc462d92188a")
        .cookie(
            " jieqiUserInfo",
            "jieqiUserId%3D1125456%2CjieqiUserName%3Dyyhyy%2CjieqiUserGroup%3D3%2CjieqiUserVip%3D0%2CjieqiUserPassword%3Deb62861281462fd923fb99218735fef0%2CjieqiUserName_un%3Dyyhyy%2CjieqiUserHonor_un%3D%26%23x666E%3B%26%23x901A%3B%26%23x4F1A%3B%26%23x5458%3B%2CjieqiUserGroupName_un%3D%26%23x666E%3B%26%23x901A%3B%26%23x4F1A%3B%26%23x5458%3B%2CjieqiUserLogin%3D1721745838"
        )
        .cookie(" jieqiVisitInfo", "jieqiUserLogin%3D1721745838%2CjieqiUserId%3D1125456")
        .cookie(
            " cf_clearance",
            "rAZBJvDmKV_DyAMY3k8n0_tMWW_lEz3ycWfYtjfTPcg-1721745844-1.0.1.1-mqt8uqswt6KtEdjtDq5m_yrRpR0x6QUhux3.J5B_OQMCso87cCu2psOEn0KVC1xOzmJinWcs7eeZTAi1ruNA_w"
        )
        .cookie(" HMACCOUNT", "10DAC0CE2BEFA41A")
        .cookie(" _clck", "jvuxvk%7C2%7Cfnp%7C0%7C1658")
        .cookie(" Hm_lvt_d72896ddbf8d27c750e3b365ea2fc902", "")
        .cookie(" Hm_lpvt_d72896ddbf8d27c750e3b365ea2fc902", "1721745932")
        .cookie(" _clsk", "1xyg0vc%7C1721745933282%7C2%7C1%7Co.clarity.ms%2Fcollect")

@OptIn(ExperimentalEncodingApi::class)
fun wenku8Api(request: String): Document =
    Jsoup
        .connect(update("eNpb85aBtYRBMaOkpMBKXz-xoECvPDUvu9RCLzk_Vz8xL6UoPzNFryCjAAAfiA5Q").toString())
        .data(
            "request", Base64.encode(request.toByteArray()),
            "timetoken", Instant.now().toEpochMilli().toString(),
            "appver", "1.21"
        )
        .post()
        .outputSettings(Document.OutputSettings()
            .prettyPrint(false)
            .syntax(Document.OutputSettings.Syntax.xml)
        )