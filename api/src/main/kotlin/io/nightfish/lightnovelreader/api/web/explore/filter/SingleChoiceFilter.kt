package io.nightfish.lightnovelreader.api.web.explore.filter

import io.nightfish.lightnovelreader.api.util.LocalString

/**
 * 单选过滤器
 * 允许用户从一组选项中选择一个选项作为过滤条件
 *
 * @property dialogTitle 选择对话框的标题
 * @property description 过滤器的描述文字
 *
 * @since Api 2
 */
open class SingleChoiceFilter(
    private val title: LocalString,
    val dialogTitle: LocalString,
    val description: LocalString,
    private val choices: List<String>,
    private val defaultChoice: String
) : Filter<String>(defaultChoice) {
    /**
     * 获取过滤器的显示标题
     *
     * @return 标题的本地化字符串
     *
     * @since Api 2
     */
    override fun getTitle(): LocalString = title

    /**
     * 获取所有可选项目名列表
     *
     * @return 所有可选项目的字符串列表
     *
     * @since Api 2
     */
    fun getAllChoices(): List<String> = choices

    /**
     * 获取过滤器的默认选项
     *
     * @return 默认选项的字符串
     *
     * @since Api 2
     */
    fun getDefaultChoice(): String = defaultChoice
}