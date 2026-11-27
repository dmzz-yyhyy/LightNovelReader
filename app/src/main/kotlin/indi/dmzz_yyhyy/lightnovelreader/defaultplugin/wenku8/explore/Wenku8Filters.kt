package indi.dmzz_yyhyy.lightnovelreader.defaultplugin.wenku8.explore

import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.michaelbull.result.get
import indi.dmzz_yyhyy.lightnovelreader.ui.components.FilterDropdown
import indi.dmzz_yyhyy.lightnovelreader.ui.components.SliderDialog
import io.nightfish.lightnovelreader.api.web.OptionKey
import io.nightfish.lightnovelreader.api.web.explore.filter.DataSourceFilter
import io.nightfish.lightnovelreader.api.web.explore.filter.Filter
import io.nightfish.lightnovelreader.api.web.explore.filter.LocalFilter

internal val wenku8TagOrder = OptionKey("order", "default")

internal fun wenku8OrderFilter() = DataSourceFilter(wenku8TagOrder) { value, change ->
    val choices = listOf("default" to "默认", "updated" to "更新时间", "popular" to "热度", "animated" to "仅动画化")
    FilterDropdown("排序: ${choices.toMap().getValue(value)}", value != wenku8TagOrder.defaultValue, choices, change)
}

internal fun wenku8LocalFilters(): List<Filter<*>> = listOf(completedFilter(), publishingHouseFilter(), wordCountFilter())

private fun completedFilter() = LocalFilter(
    key = OptionKey("completed", false),
    renderer = { selected, change ->
        FilterChip(selected = selected, onClick = { change(!selected) }, label = { Text("已完结") })
    },
    matches = { selected, book -> !selected || book.information.get()?.isComplete == true }
)

private fun publishingHouseFilter(): LocalFilter<String> {
    val choices = listOf(
        "" to "全部轻小说",
        "电击文库" to "电击文库", "富士见文库" to "富士见文库", "角川文库" to "角川文库",
        "MF文库J" to "MF文库J", "Fami通文库" to "Fami通文库", "GA文库" to "GA文库",
        "HJ文库" to "HJ文库", "一迅社" to "一迅社", "集英社" to "集英社", "小学馆" to "小学馆",
        "讲谈社" to "讲谈社", "少女文库" to "少女文库", "其他文库" to "其他文库", "游戏剧本" to "游戏剧本"
    )
    return LocalFilter(
        key = OptionKey("publishing_house", ""),
        renderer = { value, change ->
            FilterDropdown("文库: ${choices.toMap().getValue(value)}", value.isNotEmpty(), choices, change)
        },
        matches = { value, book -> value.isEmpty() || book.information.get()?.publishingHouse?.trim() == value }
    )
}

private fun wordCountFilter() = LocalFilter(
    key = OptionKey("min_word_count", 0),
    renderer = { value, change ->
        var visible by remember { mutableStateOf(false) }
        var draft by remember { mutableFloatStateOf(0f) }
        FilterChip(
            selected = value > 0,
            label = { Text(if (value == 0) "字数" else "≥ ${value / 10_000} 万字") },
            onClick = {
                draft = value.toFloat()
                visible = true
            }
        )
        if (visible) {
            SliderDialog(
                onDismissRequest = { visible = false },
                onConfirmation = {
                    visible = false
                    change(draft.toInt())
                },
                value = draft,
                valueRange = 0f..2_000_000f,
                steps = 9,
                onSlideChange = { draft = it },
                onSliderChangeFinished = {},
                title = "最低字数",
                description = "设为 0 时显示不限字数的书目。"
            )
        }
    },
    matches = { value, book -> value == 0 || (book.information.get()?.wordCount?.count ?: -1) >= value }
)
