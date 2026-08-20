package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore.expanedpage.filter

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.util.local
import io.nightfish.lightnovelreader.api.web.explore.filter.LocalFilter
import io.nightfish.lightnovelreader.api.web.explore.filter.SingleChoiceFilter

class PublishingHouseSingleChoiceFilter : SingleChoiceFilter(
    title = "文库".local(),
    dialogTitle = "文库筛选".local(),
    description = "根据小说的文库筛选".local(),
    choices = listOf(
        "全部轻小说",
        "电击文库",
        "富士见文库",
        "角川文库",
        "MF文库J",
        "Fami通文库",
        "GA文库",
        "HJ文库",
        "一迅社",
        "集英社",
        "小学馆",
        "讲谈社",
        "少女文库",
        "其他文库",
        "游戏剧本"
    ),
    defaultChoice = "全部轻小说"
), LocalFilter {
    override fun filter(bookInformation: BookInformation): Boolean =
        value == this.getDefaultChoice() || bookInformation.publishingHouse == this.value
}