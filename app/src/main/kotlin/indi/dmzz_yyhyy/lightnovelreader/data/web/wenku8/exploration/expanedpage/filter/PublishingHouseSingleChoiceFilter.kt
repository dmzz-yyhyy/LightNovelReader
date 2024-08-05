package indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.exploration.expanedpage.filter

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.LocalFilter
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.filter.SingleChoiceFilter

class PublishingHouseSingleChoiceFilter(onChange: (String) -> Unit) : SingleChoiceFilter(
    title = "文库",
    dialogTitle = "文库筛选",
    description = "根据小说文库的筛选。",
    choices = listOf("全部轻小说", "电击文库", "富士见文库", "角川文库", "MF文库J", "Fami通文库", "GA文库", "HJ文库", "一迅社", "集英社", "小学馆", "讲谈社", "少女文库", "其他文库", "游戏剧本"),
    defaultChoice = "全部轻小说",
    onChange = onChange
), LocalFilter {
    override fun filter(bookInformation: BookInformation): Boolean =
        choice == this.getDefaultChoice() || bookInformation.publishingHouse == this.choice
}