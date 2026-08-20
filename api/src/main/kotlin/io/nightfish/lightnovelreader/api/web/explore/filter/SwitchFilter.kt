package io.nightfish.lightnovelreader.api.web.explore.filter

import io.nightfish.lightnovelreader.api.util.LocalString

/**
 * 开关过滤器的抽象基类
 * 封装以布尔开关为过滤条件的过滤器
 *
 * @since Api 2
 */
abstract class SwitchFilter(
    private var title: LocalString,
    default: Boolean
) : Filter<Boolean>(default) {
    /**
     * 获取过滤器的显示标题
     *
     * @return 标题的本地化字符串
     *
     * @since Api 2
     */
    override fun getTitle(): LocalString = title
}