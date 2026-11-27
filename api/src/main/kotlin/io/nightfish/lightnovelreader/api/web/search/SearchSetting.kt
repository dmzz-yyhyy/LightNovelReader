package io.nightfish.lightnovelreader.api.web.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
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
import io.nightfish.lightnovelreader.api.util.LocalString
import io.nightfish.lightnovelreader.api.web.OptionKey
import io.nightfish.lightnovelreader.api.web.OptionValues
import java.util.Collections

/**
 * 搜索设置中的候选项。
 *
 * @property id 请求中传递的稳定值
 * @property label 显示给读者的名称
 */
data class SearchOption(val id: String, val label: LocalString)

/**
 * 由宿主在搜索设置底栏中展示的选项。
 *
 * @param T 搜索设置状态的类型
 * @property key 当前选项使用的类型化键
 * @property title 此设置的标题
 */
sealed class SearchSetting<T : Any>(val key: OptionKey<T>, val title: LocalString) {
    /**
     * 显示设置并将编辑合并到最新快照。
     *
     * @param values 获取最新的搜索设置
     * @param onChange 接收编辑后的设置快照
     */
    @Composable
    fun Content(values: () -> OptionValues, onChange: (OptionValues) -> Unit) {
        Render(values()[key]) { onChange(values().with(key, it)) }
    }

    /**
     * 显示对应类型的控件。
     *
     * @param value 当前值
     * @param onChange 提交编辑后的值
     */
    @Composable
    protected abstract fun Render(value: T, onChange: (T) -> Unit)

    /**
     * 将全部候选项展示为单选 Chips。
     *
     * @param id 设置标识
     * @param title 设置名称
     * @param options 候选项
     * @param defaultId 默认候选项标识
     */
    class SingleChoice(
        id: String,
        title: LocalString,
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
     * 将候选项展示为可独立选择的 Chips。
     *
     * @param id 设置标识
     * @param title 设置名称
     * @param options 候选项
     * @param defaultIds 默认选中的候选项标识
     */
    class MultipleChoice(
        id: String,
        title: LocalString,
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
     * 通过开关编辑布尔值。
     *
     * @param id 设置标识
     * @param title 设置名称
     * @param defaultValue 开关的默认状态
     */
    class Toggle(id: String, title: LocalString, defaultValue: Boolean = false) :
        SearchSetting<Boolean>(OptionKey(id, defaultValue), title) {
        @Composable
        override fun Render(value: Boolean, onChange: (Boolean) -> Unit) {
            ListItem(trailingContent = { Switch(checked = value, onCheckedChange = onChange) }) {
                Text(title.resolve())
            }
        }
    }

    /**
     * 在设置行中显示当前项，打开下拉菜单后选择一个候选项。
     *
     * @param id 设置标识
     * @param title 设置名称
     * @param options 候选项
     * @param defaultId 默认候选项标识
     */
    class ListChoice(
        id: String,
        title: LocalString,
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
                            Text("${current.label.resolve()} ▾")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            choices.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label.resolve()) },
                                    onClick = {
                                        expanded = false
                                        onChange(option.id)
                                    }
                                )
                            }
                        }
                    }
                }
            ) { Text(title.resolve()) }
        }
    }
}

@Composable
private fun ChoiceRow(
    title: LocalString,
    choices: List<SearchOption>,
    selected: (String) -> Boolean,
    onClick: (String) -> Unit
) {
    Column {
        Text(title.resolve(), style = MaterialTheme.typography.titleSmall)
        FlowRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            choices.forEach { option ->
                FilterChip(
                    selected = selected(option.id),
                    onClick = { onClick(option.id) },
                    label = { Text(option.label.resolve()) }
                )
            }
        }
    }
}
