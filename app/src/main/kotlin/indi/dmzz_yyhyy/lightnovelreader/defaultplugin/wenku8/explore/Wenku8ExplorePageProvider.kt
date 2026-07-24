package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore

import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.expanedpage.HomeBookExpandPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.expanedpage.filter.PublishingHouseSingleChoiceFilter
import io.nightfish.lightnovelreader.api.util.local
import io.nightfish.lightnovelreader.api.web.explore.AbstractDefaultExplorePageProvider
import io.nightfish.lightnovelreader.api.web.explore.filter.IsCompletedSwitchFilter
import io.nightfish.lightnovelreader.api.web.explore.filter.SingleChoiceFilter
import io.nightfish.lightnovelreader.api.web.explore.filter.WordCountFilter
import java.net.URLEncoder

class Wenku8ExplorePageProvider(
    val host: String,
    val wenku8Api: Wenku8Api
): AbstractDefaultExplorePageProvider() {
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

        registerExpandedPageDataSource(
            id = "allBook",
            exploreExpandedPageDataSource = HomeBookExpandPageDataSource(
                host = host,
                wenku8Api = wenku8Api,
                title = "轻小说列表",
                filtersBuilder = {
                    listOf(
                        IsCompletedSwitchFilter(),
                        PublishingHouseSingleChoiceFilter(),
                        WordCountFilter()
                    )
                },
            )
        )
        registerExpandedPageDataSource(
            id = "allCompletedBook",
            exploreExpandedPageDataSource = HomeBookExpandPageDataSource(
                host = host,
                wenku8Api = wenku8Api,
                title = "完结全本",
                filtersBuilder = {
                    listOf(
                        IsCompletedSwitchFilter(),
                        PublishingHouseSingleChoiceFilter(),
                        WordCountFilter()
                    )
                },
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
            registerExpandedPageDataSource(
                id = "${id}Book",
                exploreExpandedPageDataSource = HomeBookExpandPageDataSource(
                    host = host,
                    wenku8Api = wenku8Api,
                    baseUrl = "$host/modules/article/toplist.php",
                    title = (nameMap[id] ?: ""),
                    filtersBuilder = {
                        listOf(
                            IsCompletedSwitchFilter(),
                            PublishingHouseSingleChoiceFilter(),
                            WordCountFilter()
                        )
                    },
                    extendedParameters = "&sort=$id",
                    contentSelector = "#content > table > tbody > tr > td > div"
                )
            )
        }
        tagList.forEach { tag ->
            registerExpandedPageDataSource(
                id = tag,
                exploreExpandedPageDataSource = HomeBookExpandPageDataSource(
                    host = host,
                    wenku8Api = wenku8Api,
                    baseUrl = "$host/modules/article/tags.php",
                    title = tag,
                    filtersBuilder = {
                        val choicesMap = mapOf(
                            Pair("默认", ""),
                            Pair("按更新时间排序", ""),
                            Pair("按热度排序", "&v=1"),
                            Pair("仅动画化", "&v=3")
                        )
                        listOf(
                            IsCompletedSwitchFilter(),
                            SingleChoiceFilter(
                                title = "排序".local(),
                                dialogTitle = "文库筛选".local(),
                                description = "根据小说的文库筛选".local(),
                                choices = listOf(
                                    "默认",
                                    "按更新时间排序",
                                    "按热度排序",
                                    "仅动画化"
                                ),
                                defaultChoice = "默认"
                            ).apply {
                                addOnChangeListener {
                                    this@HomeBookExpandPageDataSource.arg = choicesMap[it.trim()] ?: ""
                                }
                            },
                            PublishingHouseSingleChoiceFilter(),
                            WordCountFilter()
                        )
                    },
                    extendedParameters = "&t=${URLEncoder.encode(tag, "gb2312")}",
                    contentSelector = "#content > table > tbody > tr:nth-child(2) > td > div"
                )
            )
        }
    }
}