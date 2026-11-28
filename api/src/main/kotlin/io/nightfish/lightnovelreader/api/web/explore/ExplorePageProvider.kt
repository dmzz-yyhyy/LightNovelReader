package io.nightfish.lightnovelreader.api.web.explore

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import kotlinx.coroutines.CoroutineScope

/**
 * 探索页面内容提供器的封闭接口
 * 支持使用宿主默认布局或自定义整个探索页面内容
 *
 * @since Api 2
 */
sealed interface ExplorePageProvider {
    /**
     * 使用宿主默认布局的探索页面提供器
     * 可同时注册由宿主渲染的页面和由插件渲染内容的自定义页面
     *
     * @since Api 2
     */
    interface DefaultExplorePageProvider : ExplorePageProvider {
        /**
         * 以页面标识为键的探索卡片页映射
         * 映射的迭代顺序决定标签栏的显示顺序
         *
         * @since Api 4
         */
        val exploreTapPages: Map<String, ExploreTapPage>

        /**
         * 以路由标识为键的探索展开页映射
         *
         * @since Api 4
         */
        val exploreExpandedPages: Map<String, ExploreExpandedPage>
    }

    /**
     * 完全自定义探索页面内容的接口
     * 实现者可自行决定页面布局和 UI 状态
     *
     * @param T 探索页面 UI 状态的类型
     *
     * @since Api 2
     */
    interface CustomExplorePageProvider<T> : ExplorePageProvider {
        /**
         * 探索页面的 UI 状态数据
         *
         * @since Api 2
         */
        val uiState: T

        /**
         * 初始化探索页面的数据载入逻辑
         *
         * @param viewModelScope 绑定到页面生命周期的协程作用域
         *
         * @since Api 2
         */
        fun init(viewModelScope: CoroutineScope)

        /**
         * 渲染探索页面的展示内容
         *
         * @param nestedScrollConnection 用于协调页面和宿主顶栏滚动的嵌套滚动连接
         *
         * @since Api 2
         */
        @Composable
        fun Content(nestedScrollConnection: NestedScrollConnection)
    }
}
