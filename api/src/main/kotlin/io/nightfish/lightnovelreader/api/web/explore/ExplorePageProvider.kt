package io.nightfish.lightnovelreader.api.web.explore

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import kotlinx.coroutines.CoroutineScope

/**
 * 探索页面内容提供器的封闭接口
 * 共有两种实现方式：使用软件默认布局或完全自定义页面内容
 *
 * @since Api 2
 */
sealed interface ExplorePageProvider {
    /**
     * 软件默认实现的探索页面
     * 使用卡片页与展开页島效果组合的默认布局
     *
     * @since Api 2
     */
    interface DefaultExplorePageProvider : ExplorePageProvider {
        /** 已注册的探索页面 ID 有序列表 @since Api 2 */
        val explorePageIdList: List<String>

        /** 以页面 ID 为键的探索卡片页数据源映射 @since Api 2 */
        val exploreTapPageDataSourceMap: Map<String, ExploreTapPageDataSource>

        /** 以页面 ID 为键的探索展开页数据源映射 @since Api 2 */
        val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource>
    }

    /**
     * 完全自定义探索页面内容
     * 实现者可自行决定页面布局和UI状态
     *
     * @since Api 2
     */
    interface CustomExplorePageProvider<T> : ExplorePageProvider {
        /** 探索页面的 UI 状态数据 @since Api 2 */
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
         * @param nestedScrollConnection 嵌套滚动连接器，用于协调滚动事件
         *
         * @since Api 2
         */
        @Composable
        fun Content(nestedScrollConnection: NestedScrollConnection)
    }
}