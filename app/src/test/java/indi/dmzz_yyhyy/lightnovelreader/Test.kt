package indi.dmzz_yyhyy.lightnovelreader

import java.time.format.DateTimeFormatter
import org.jsoup.Connection
import org.jsoup.Jsoup

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

private val DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun main() {
    println(Jsoup.parse("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<package>\n" +
            "<volume vid=\"139289\"><![CDATA[第一卷]]>\n" +
            "<chapter cid=\"139290\"><![CDATA[序幕 相当微小的，宛如梦境的美丽记忆]]></chapter>\n" +
            "<chapter cid=\"139291\"><![CDATA[第一章 从社畜超越时空回到那个时候]]></chapter>\n" +
            "<chapter cid=\"139292\"><![CDATA[第二章 重新开始第二次的青春]]></chapter>\n" +
            "<chapter cid=\"139293\"><![CDATA[第三章 放学后与憧憬的少女一起]]></chapter>\n" +
            "<chapter cid=\"139294\"><![CDATA[幕间休息 紫条院春华的呢喃]]></chapter>\n" +
            "<chapter cid=\"139295\"><![CDATA[第四章 校园阶层等级上升中]]></chapter>\n" +
            "<chapter cid=\"139296\"><![CDATA[第五章 社畜的简报]]></chapter>\n" +
            "<chapter cid=\"139297\"><![CDATA[第六章 能者多劳]]></chapter>\n" +
            "<chapter cid=\"139298\"><![CDATA[第七章 春华的挑战与情书]]></chapter>\n" +
            "<chapter cid=\"139299\"><![CDATA[第八章 与你共度校庆]]></chapter>\n" +
            "<chapter cid=\"139300\"><![CDATA[第九章 修罗般的现场才是社畜的勋章]]></chapter>\n" +
            "<chapter cid=\"139301\"><![CDATA[第十章 对功劳者说声「谢谢」]]></chapter>\n" +
            "<chapter cid=\"139302\"><![CDATA[第十一章 恶梦与腿枕]]></chapter>\n" +
            "<chapter cid=\"139303\"><![CDATA[终幕 走过这奇迹般的日子]]></chapter>\n" +
            "<chapter cid=\"139304\"><![CDATA[后记]]></chapter>\n" +
            "<chapter cid=\"140986\"><![CDATA[电子书特典〈毫无防备的紫条院同学〉]]></chapter>\n" +
            "<chapter cid=\"140987\"><![CDATA[插图]]></chapter>\n" +
            "</volume>\n" +
            "<volume vid=\"139559\"><![CDATA[第二卷]]>\n" +
            "<chapter cid=\"139560\"><![CDATA[序幕 校庆当晚的紫条院家]]></chapter>\n" +
            "<chapter cid=\"139561\"><![CDATA[第一章 校园阶层顶端的自大型帅哥来找碴]]></chapter>\n" +
            "<chapter cid=\"139924\"><![CDATA[第二章 现正用功中！]]></chapter>\n" +
            "<chapter cid=\"140202\"><![CDATA[第三章 考试比赛的结局]]></chapter>\n" +
            "<chapter cid=\"140237\"><![CDATA[第四章 无关胜负的必然结果]]></chapter>\n" +
            "<chapter cid=\"140344\"><![CDATA[第五章 前往紫条院家的招待]]></chapter>\n" +
            "<chapter cid=\"141121\"><![CDATA[第六章 落得必须向心仪女孩的妈妈告白心意的下场]]></chapter>\n" +
            "<chapter cid=\"141658\"><![CDATA[第七章 社长VS社畜]]></chapter>\n" +
            "<chapter cid=\"141975\"><![CDATA[第八章 在那个女孩的房里举行茶会]]></chapter>\n" +
            "<chapter cid=\"142024\"><![CDATA[终幕1 战果报告后妹妹的腹肌坏死]]></chapter>\n" +
            "<chapter cid=\"142030\"><![CDATA[终幕2 脚步稍微前进，夏天即将到来]]></chapter>\n" +
            "<chapter cid=\"142031\"><![CDATA[后记]]></chapter>\n" +
            "<chapter cid=\"145515\"><![CDATA[电子书特典〈明明只是问个信箱而已〉]]></chapter>\n" +
            "<chapter cid=\"145516\"><![CDATA[插图]]></chapter>\n" +
            "</volume>\n" +
            "<volume vid=\"143962\"><![CDATA[第三卷]]>\n" +
            "<chapter cid=\"143963\"><![CDATA[序幕 传简讯给那个女孩]]></chapter>\n" +
            "<chapter cid=\"145305\"><![CDATA[第一章 球技大会的记忆与运动白痴的奋斗]]></chapter>\n" +
            "<chapter cid=\"145444\"><![CDATA[第二章 拼尽全力而香汗淋漓的天使]]></chapter>\n" +
            "<chapter cid=\"145532\"><![CDATA[第三章 现在这个时候只需要热血]]></chapter>\n" +
            "<chapter cid=\"145603\"><![CDATA[幕间 暑假开始]]></chapter>\n" +
            "<chapter cid=\"145694\"><![CDATA[第四章 这次绝对不背叛妹妹的笑容]]></chapter>\n" +
            "<chapter cid=\"146252\"><![CDATA[第五章 紫条院春华的嫉妒]]></chapter>\n" +
            "<chapter cid=\"146328\"><![CDATA[第六章 春华与香奈子]]></chapter>\n" +
            "<chapter cid=\"146487\"><![CDATA[第七章 春华受到新滨家的欢迎]]></chapter>\n" +
            "<chapter cid=\"147858\"><![CDATA[第八章 可以住下来吗？]]></chapter>\n" +
            "<chapter cid=\"148203\"><![CDATA[第九章 深夜的茶会与完事后的早晨]]></chapter>\n" +
            "<chapter cid=\"148864\"><![CDATA[终幕1 在夏日的早晨面临暂时的离别]]></chapter>\n" +
            "<chapter cid=\"148981\"><![CDATA[终幕2 同一时刻，回想过去的那一夜]]></chapter>\n" +
            "<chapter cid=\"148982\"><![CDATA[后记]]></chapter>\n" +
            "<chapter cid=\"149010\"><![CDATA[电子书特典〈春华与香奈子的睡衣派对〉]]></chapter>\n" +
            "<chapter cid=\"149011\"><![CDATA[插图]]></chapter>\n" +
            "<chapter cid=\"148994\"><![CDATA[电子书特典《早晨回家的大小姐的留宿报告》]]></chapter>\n" +
            "</volume>\n" +
            "<volume vid=\"149012\"><![CDATA[第四卷]]>\n" +
            "<chapter cid=\"149013\"><![CDATA[序幕 在这个灼热的夏天创造最后的回忆]]></chapter>\n" +
            "<chapter cid=\"149488\"><![CDATA[第一章 克服前往海边之前的难关]]></chapter>\n" +
            "<chapter cid=\"150172\"><![CDATA[第二章 海边的天使]]></chapter>\n" +
            "<chapter cid=\"151437\"><![CDATA[第三章 在摇摇晃晃的海洋上]]></chapter>\n" +
            "<chapter cid=\"152106\"><![CDATA[第四章 宴会也将近尾声但情况一片混乱]]></chapter>\n" +
            "<chapter cid=\"152161\"><![CDATA[终幕1 闪亮炫目的海边回忆]]></chapter>\n" +
            "<chapter cid=\"152489\"><![CDATA[终幕2 夏天结束，新的季节与阶段的开始]]></chapter>\n" +
            "<chapter cid=\"152490\"><![CDATA[后记]]></chapter>\n" +
            "<chapter cid=\"153764\"><![CDATA[插图]]></chapter>\n" +
            "<chapter cid=\"152591\"><![CDATA[电子书特典《背着醉倒的天使》]]></chapter>\n" +
            "<chapter cid=\"152592\"><![CDATA[BOOK☆WALKER特典《守护海边天使的方法》]]></chapter>\n" +
            "</volume>\n" +
            "<volume vid=\"152720\"><![CDATA[第五卷]]>\n" +
            "<chapter cid=\"152721\"><![CDATA[序章 直呼名字的新手们]]></chapter>\n" +
            "<chapter cid=\"152722\"><![CDATA[第一章 来面试打工的男高中生有点奇怪]]></chapter>\n" +
            "<chapter cid=\"152789\"><![CDATA[第二章 重回职场的社畜与光明的白色企业]]></chapter>\n" +
            "<chapter cid=\"152945\"><![CDATA[第三章 社长千金的决心]]></chapter>\n" +
            "<chapter cid=\"153041\"><![CDATA[第四章 请多指教，新滨前辈！]]></chapter>\n" +
            "<chapter cid=\"153162\"><![CDATA[第五章 过度保护的社长的秘密视察]]></chapter>\n" +
            "<chapter cid=\"153616\"><![CDATA[第六章 打工人与社长的经营战略会议]]></chapter>\n" +
            "<chapter cid=\"153819\"><![CDATA[第七章 噩梦与咖啡馆约会]]></chapter>\n" +
            "<chapter cid=\"154187\"><![CDATA[第八章 春华的成长]]></chapter>\n" +
            "<chapter cid=\"154369\"><![CDATA[第九章 在橘色浸染的世界里携手]]></chapter>\n" +
            "<chapter cid=\"154370\"><![CDATA[插图]]></chapter>\n" +
            "<chapter cid=\"154392\"><![CDATA[BOOK☆WALKER特典《夏末的线香烟花》]]></chapter>\n" +
            "</volume>\n" +
            "<volume vid=\"154616\"><![CDATA[第六卷]]>\n" +
            "<chapter cid=\"154617\"><![CDATA[第一章 幸福的庆生宴]]></chapter>\n" +
            "<chapter cid=\"154885\"><![CDATA[第二章 我从一开始就知道这种现象的名字]]></chapter>\n" +
            "</volume>\n" +
            "</package>")
        .select("volume")
        .map {
            println(it.ownText())
        }
    )
}