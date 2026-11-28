package io.nightfish.lightnovelreader.api.web.explore

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import io.nightfish.lightnovelreader.api.identifier.Identifier
import kotlinx.coroutines.CoroutineScope

/**
 * 探索卡片页的封闭接口
 * 默认探索界面可使用[ExploreTapPageDataSource]或[CustomExploreTapPage]
 *
 * @since Api 4
 */
sealed interface ExploreTapPage {
    /**
     * 卡片页的显示标题
     *
     * @since Api 4
     */
    val title: String
}

/**
 * 探索展开页的封闭接口
 * 展开页可使用[ExploreExpandedPageDataSource]或[CustomExploreExpandedPage]
 *
 * @since Api 4
 */
sealed interface ExploreExpandedPage {
    /**
     * 展开页的显示标题
     *
     * @since Api 4
     */
    val title: String
}

/**
 * 自定义探索页面的上下文
 * 用于访问宿主提供的导航操作和嵌套滚动连接
 *
 * @property nestedScrollConnection 与宿主顶栏联动的滚动连接
 * @property openBook 打开书本详情
 * @property openExpandedPage 根据页面标识打开同一个[ExplorePageProvider]中已注册的展开页
 * @property openSearch 打开当前数据源的搜索页
 *
 * @since Api 4
 */
class ExplorePageContext(
    val nestedScrollConnection: NestedScrollConnection,
    val openBook: (Identifier.Book) -> Unit,
    val openExpandedPage: (String) -> Unit,
    val openSearch: () -> Unit
)

/**
 * 自定义探索页面内容的接口
 * 宿主负责页面生命周期和导航，插件负责渲染页面内容
 *
 * @since Api 4
 */
interface CustomExplorePageContent {
    /**
     * 初始化探索页面的数据载入逻辑
     * 页面离开时，宿主会取消该协程作用域
     *
     * @param scope 绑定到页面生命周期的协程作用域
     *
     * @since Api 4
     */
    fun init(scope: CoroutineScope) = Unit

    /**
     * 渲染宿主布局内的自定义探索页面内容
     *
     * @param context 包含导航操作和嵌套滚动连接的页面上下文
     *
     * @since Api 4
     */
    @Composable
    fun Content(context: ExplorePageContext)
}

/**
 * 自定义探索卡片页的接口
 * 页面内容显示在宿主的标签栏下方
 *
 * @since Api 4
 */
interface CustomExploreTapPage : ExploreTapPage, CustomExplorePageContent

/**
 * 自定义探索展开页的接口
 * 页面内容显示在宿主的展开页顶栏下方
 *
 * @since Api 4
 */
interface CustomExploreExpandedPage : ExploreExpandedPage, CustomExplorePageContent
