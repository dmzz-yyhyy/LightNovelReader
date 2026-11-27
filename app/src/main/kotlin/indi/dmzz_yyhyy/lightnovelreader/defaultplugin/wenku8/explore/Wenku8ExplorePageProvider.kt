package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore

import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.expanedpage.HomeBookExpandPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.AbstractDefaultExplorePageProvider
import java.net.URLEncoder

class Wenku8ExplorePageProvider(
    val host: String,
    val wenku8Api: Wenku8Api
) : AbstractDefaultExplorePageProvider() {
    private val tagQueryOptions = mapOf("default" to "", "updated" to "", "popular" to "&v=1", "animated" to "&v=3")
    private val tagList = listOf(
        "校园", "青春", "恋爱", "治愈", "群像",
        "竞技", "音乐", "美食", "旅行", "欢乐向",
        "经营", "职场", "斗智", "脑洞", "宅文化",
        "穿越", "奇幻", "魔法", "异能", "战斗",
        "科幻", "机战", "战争", "冒险", "龙傲天",
        "悬疑", "犯罪", "复仇", "黑暗", "猎奇",
        "惊悚", "间谍", "末日", "游戏", "大逃杀",
        "青梅竹马", "妹妹", "女儿", "JK", "JC",
        "大小姐", "性转", "伪娘", "人外",
        "后宫", "百合", "耽美", "NTR", "女性视角"
    )

    init {
        registerTapPage(Wenku8HomeExploreTapPage(host, wenku8Api))
        registerTapPage(Wenku8AllExploreTapPage(host, wenku8Api))
        registerTapPage(Wenku8TagsExploreTapPage(host, wenku8Api))

        registerExpandedPage(
            id = "allBook",
            page = HomeBookExpandPageDataSource(
                host = host,
                wenku8Api = wenku8Api,
                title = "轻小说列表",
                filters = wenku8LocalFilters(),
            )
        )
        registerExpandedPage(
            id = "allCompletedBook",
            page = HomeBookExpandPageDataSource(
                host = host,
                wenku8Api = wenku8Api,
                title = "完结全本",
                filters = wenku8LocalFilters(),
                extendedParameters = "&fullflag=1"
            )
        )
        listOf("allvisit", "anime", "lastupdate", "postdate").forEach { id ->
            val nameMap = mapOf(
                Pair("allvisit", "热门轻小说"),
                Pair("anime", "动画化作品"),
                Pair("lastupdate", "今日更新"),
                Pair("postdate", "新书一览"),
            )
            registerExpandedPage(
                id = "${id}Book",
                page = HomeBookExpandPageDataSource(
                    host = host,
                    wenku8Api = wenku8Api,
                    baseUrl = "$host/modules/article/toplist.php",
                    title = (nameMap[id] ?: ""),
                    filters = wenku8LocalFilters(),
                    extendedParameters = "&sort=$id",
                    contentSelector = "#content > table > tbody > tr > td > div"
                )
            )
        }
        tagList.forEach { tag ->
            registerExpandedPage(
                id = tag,
                page = HomeBookExpandPageDataSource(
                    host = host,
                    wenku8Api = wenku8Api,
                    baseUrl = "$host/modules/article/tags.php",
                    title = tag,
                    filters = wenku8LocalFilters().toMutableList().apply { add(1, wenku8OrderFilter()) },
                    queryOptions = { options -> tagQueryOptions.getValue(options[wenku8TagOrder]) },
                    extendedParameters = "&t=${URLEncoder.encode(tag, "gb2312")}",
                    contentSelector = "#content > table > tbody > tr:nth-child(2) > td > div"
                )
            )
        }
    }
}