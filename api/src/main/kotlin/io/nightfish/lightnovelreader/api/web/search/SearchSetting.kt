package io.nightfish.lightnovelreader.api.web.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.nightfish.lightnovelreader.api.web.OptionKey
import io.nightfish.lightnovelreader.api.web.OptionValues
import java.util.Collections

/**
 * 搜索设置候选项的数据类
 *
 * @property id 候选项的唯一标识，用于在请求中传递选中的值
 * @property label 在 Compose 中获取候选项本地化显示名称的函数
 *
 * @since Api 4
 */
data class SearchOption(val id: String, val label: @Composable () -> String)

/**
 * 搜索设置的抽象基类
 * 子类封装具体的设置类型及对应的界面控件
 *
 * @param T 搜索设置持有值的类型
 * @property key 搜索设置使用的选项键
 * @property title 在 Compose 中获取搜索设置本地化显示标题的函数
 *
 * @since Api 4
 */
sealed class SearchSetting<T : Any>(val key: OptionKey<T>, val title: @Composable () -> String) {
    /**
     * 渲染搜索设置
     * 提交修改时，会将更新合并到最新的搜索设置快照
     *
     * @param values 用于读取最新搜索设置快照的函数
     * @param onChange 接收更新后的搜索设置快照的回调
     *
     * @since Api 4
     */
    @Composable
    fun Content(values: () -> OptionValues, onChange: (OptionValues) -> Unit) {
        Render(values()[key]) { onChange(values().with(key, it)) }
    }

    /**
     * 渲染当前设置类型对应的控件
     *
     * @param value 当前设置值
     * @param onChange 接收修改后的设置值的回调
     *
     * @since Api 4
     */
    @Composable
    protected abstract fun Render(value: T, onChange: (T) -> Unit)

    /**
     * 单选搜索设置
     * 将所有候选项直接展示为筛选标签，同一时间只能选中一项
     *
     * @param id 设置的唯一标识
     * @param title 在 Compose 中获取设置本地化显示标题的函数
     * @param options 可供选择的候选项列表
     * @param defaultId 默认选中的候选项标识
     * @throws IllegalArgumentException 默认标识未出现在候选项中
     *
     * @since Api 4
     */
    class SingleChoice(
        id: String,
        title: @Composable () -> String,
        options: List<SearchOption>,
        defaultId: String
    ) : SearchSetting<String>(OptionKey(id, defaultId), title) {
        private val choices = options.toList()

        init {
            require(choices.any { it.id == defaultId })
        }

        @Composable
        override fun Render(value: String, onChange: (String) -> Unit) {
            ChoiceRow(title, choices, { it == value }, onChange)
        }
    }

    /**
     * 多选搜索设置
     * 将所有候选项展示为可以独立选中的筛选标签
     *
     * @param id 设置的唯一标识
     * @param title 在 Compose 中获取设置本地化显示标题的函数
     * @param options 可供选择的候选项列表
     * @param defaultIds 默认选中的候选项标识集合
     * @throws IllegalArgumentException 任一默认标识未出现在候选项中
     *
     * @since Api 4
     */
    class MultipleChoice(
        id: String,
        title: @Composable () -> String,
        options: List<SearchOption>,
        defaultIds: Set<String> = emptySet()
    ) : SearchSetting<Set<String>>(
        OptionKey(id, defaultIds) { Collections.unmodifiableSet(LinkedHashSet(it)) },
        title
    ) {
        private val choices = options.toList()

        init {
            require(choices.map { it.id }.containsAll(defaultIds))
        }

        @Composable
        override fun Render(value: Set<String>, onChange: (Set<String>) -> Unit) {
            ChoiceRow(title, choices, { it in value }) { id ->
                onChange(if (id in value) value - id else value + id)
            }
        }
    }

    /**
     * 布尔搜索设置
     * 使用开关展示和修改设置状态
     *
     * @param id 设置的唯一标识
     * @param title 在 Compose 中获取设置本地化显示标题的函数
     * @param defaultValue 开关的默认状态
     *
     * @since Api 4
     */
    class Toggle(id: String, title: @Composable () -> String, defaultValue: Boolean = false) :
        SearchSetting<Boolean>(OptionKey(id, defaultValue), title) {
        @Composable
        override fun Render(value: Boolean, onChange: (Boolean) -> Unit) {
            ListItem(trailingContent = { Switch(checked = value, onCheckedChange = onChange) }) {
                Text(title())
            }
        }
    }

    /**
     * 列表项搜索设置
     * 在设置行中显示当前项，用户可通过下拉菜单选择其他候选项
     *
     * @param id 设置的唯一标识
     * @param title 在 Compose 中获取设置本地化显示标题的函数
     * @param options 可供选择的候选项列表
     * @param defaultId 默认选中的候选项标识
     * @throws IllegalArgumentException 默认标识未出现在候选项中
     *
     * @since Api 4
     */
    class ListChoice(
        id: String,
        title: @Composable () -> String,
        options: List<SearchOption>,
        defaultId: String
    ) : SearchSetting<String>(OptionKey(id, defaultId), title) {
        private val choices = options.toList()

        init {
            require(choices.any { it.id == defaultId })
        }

        @Composable
        override fun Render(value: String, onChange: (String) -> Unit) {
            var expanded by remember { mutableStateOf(false) }
            ListItem(
                trailingContent = {
                    Box {
                        FilledTonalButton(onClick = { expanded = true }) {
                            val current = choices.firstOrNull { it.id == value } ?: choices.first()
                            Text("${current.label()} ▾")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            choices.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label()) },
                                    onClick = {
                                        expanded = false
                                        onChange(option.id)
                                    }
                                )
                            }
                        }
                    }
                }
            ) { Text(title()) }
        }
    }
}

@Composable
private fun ChoiceRow(
    title: @Composable () -> String,
    choices: List<SearchOption>,
    selected: (String) -> Boolean,
    onClick: (String) -> Unit
) {
    Column {
        Text(title(), style = MaterialTheme.typography.titleSmall)
        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            choices.forEach { option ->
                FilterChip(
                    selected = selected(option.id),
                    onClick = { onClick(option.id) },
                    label = { Text(option.label()) }
                )
            }
        }
    }
}
