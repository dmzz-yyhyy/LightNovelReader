package io.nightfish.lightnovelreader.api.web.explore.filter

import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.util.local

/**
 * “已完结”开关过滤器
 * 用于本地过滤，开启后仅显示已标记为已完结的书本
 *
 * @since Api 2
 */
class IsCompletedSwitchFilter : SwitchFilter("\u5df2\u5b8c\u7ed3".local(), false), LocalFilter {
    override fun filter(bookInformation: BookInformation): Boolean =
        !this.value || bookInformation.isComplete
}