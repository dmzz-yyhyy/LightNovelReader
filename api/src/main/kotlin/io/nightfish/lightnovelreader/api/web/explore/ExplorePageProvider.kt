package io.nightfish.lightnovelreader.api.web.explore

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import kotlinx.coroutines.CoroutineScope

/** 提供宿主默认探索界面或整页自定义界面。 */
sealed interface ExplorePageProvider {
    /** 使用宿主提供的顶栏和标签栏。可混合注册标准页与自定义页。 */
    interface DefaultExplorePageProvider : ExplorePageProvider {
        /** 以页面标识为键的标签页，迭代顺序决定标签栏顺序。 */
        val exploreTapPages: Map<String, ExploreTapPage>

        /** 以路由标识为键的展开页。 */
        val exploreExpandedPages: Map<String, ExploreExpandedPage>
    }

    /**
     * 自定义整个探索内容区域。
     *
     * @param T 自定义界面状态的类型
     */
    interface CustomExplorePageProvider<T> : ExplorePageProvider {
        /** 自定义界面的状态。 */
        val uiState: T

        /**
         * 初始化探索内容。
         *
         * @param viewModelScope 宿主提供的页面协程作用域
         */
        fun init(viewModelScope: CoroutineScope)

        /**
         * 渲染自定义探索内容。
         *
         * @param nestedScrollConnection 与宿主顶栏联动的滚动连接
         */
        @Composable
        fun Content(nestedScrollConnection: NestedScrollConnection)
    }
}
