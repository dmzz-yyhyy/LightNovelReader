package io.nightfish.lightnovelreader.api.web.explore

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.coroutines.CoroutineScope

/** 默认探索界面中的一个标签页。 */
sealed interface ExploreTapPage {
    /** 标签页标题。 */
    val title: String
}

/** 由书目行或其他探索页入口打开的展开页。 */
sealed interface ExploreExpandedPage {
    /** 展开页标题。 */
    val title: String
}

/**
 * 宿主提供给自定义页面内容的导航和滚动接口。
 *
 * @property nestedScrollConnection 与宿主顶栏联动的滚动连接
 * @property openBook 打开书本详情
 * @property openExpandedPage 打开同一个 Provider 下已注册的展开页
 * @property openSearch 打开当前数据源的搜索页
 */
class ExplorePageContext(
    val nestedScrollConnection: NestedScrollConnection,
    val openBook: (Identifier.Book) -> Unit,
    val openExpandedPage: (String) -> Unit,
    val openSearch: () -> Unit
)

/** 自定义探索内容，可在页面激活期间使用宿主提供的协程作用域。 */
interface CustomExplorePageContent {
    /**
     * 初始化页面。页面离开时宿主取消作用域。
     *
     * @param scope 当前页面的协程作用域
     */
    fun init(scope: CoroutineScope) = Unit

    /**
     * 渲染顶栏和导航之间的页面内容。
     *
     * @param context 宿主提供的导航回调和滚动连接
     */
    @Composable
    fun Content(context: ExplorePageContext)
}

/** 在宿主标签栏下渲染自定义内容。 */
interface CustomExploreTapPage : ExploreTapPage, CustomExplorePageContent

/** 在宿主展开页顶栏下渲染自定义内容。 */
interface CustomExploreExpandedPage : ExploreExpandedPage, CustomExplorePageContent
