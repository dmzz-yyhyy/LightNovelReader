package io.nightfish.lightnovelreader.api.plugin

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/**
 * LightNovelReader 插件接口
 * 所有插件需实现此接口, 并使用[@Plugin]注解标注元数据
 * 支持依赖注入
 *
 * @since Api 2
 */
interface LightNovelReaderPlugin {
    /**
     * 插件被加载时回调
     *
     * @since Api 2
     */
    fun onLoad() {}

    /**
     * 插件被卸载时回调
     *
     * @since Api 2
     */
    fun onUnload() {}

    /**
     * 当软件初始化导航时调用
     * 仅当软件首次启动时有效
     *
     * @since Api 2
     */
    fun EntryProviderScope<NavKey>.onBuildNavHost() {}

    /**
     * 插件向软件注入的页面内容
     * 不需要使用时可以不实现
     *
     * @param paddingValues 系统内边距
     *
     * @since Api 2
     */
    @Composable
    fun PageContent(paddingValues: PaddingValues) {
    }
}