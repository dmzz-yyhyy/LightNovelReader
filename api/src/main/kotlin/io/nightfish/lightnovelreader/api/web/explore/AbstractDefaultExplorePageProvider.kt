package io.nightfish.lightnovelreader.api.web.explore

/**
 * [ExplorePageProvider.DefaultExplorePageProvider]的默认抽象实现
 * 子类可在初始化阶段调用注册方法添加探索卡片页和展开页
 *
 * @since Api 2
 */
abstract class AbstractDefaultExplorePageProvider : ExplorePageProvider.DefaultExplorePageProvider {
    private val registeredTapPages = linkedMapOf<String, ExploreTapPage>()
    private val registeredExpandedPages = mutableMapOf<String, ExploreExpandedPage>()
    private var nextTapId = 0

    override val exploreTapPages: Map<String, ExploreTapPage> get() = registeredTapPages.toMap()
    override val exploreExpandedPages: Map<String, ExploreExpandedPage> get() = registeredExpandedPages.toMap()

    /**
     * 注册一个指定标识的探索卡片页
     *
     * @param id 页面的唯一标识
     * @param page 卡片页的数据源或自定义页面
     * @throws IllegalArgumentException 已有卡片页使用该标识
     *
     * @since Api 4
     */
    protected fun registerTapPage(id: String, page: ExploreTapPage) {
        require(id !in registeredTapPages)
        registeredTapPages[id] = page
    }

    /**
     * 注册一个探索卡片页，使用自动递增的标识
     *
     * @param page 卡片页的数据源或自定义页面
     *
     * @since Api 4
     */
    protected fun registerTapPage(page: ExploreTapPage) {
        var id: String
        do {
            id = (++nextTapId).toString()
        } while (id in registeredTapPages)
        registerTapPage(id, page)
    }

    /**
     * 注册一个指定标识的探索展开页
     *
     * @param id 页面的唯一路由标识
     * @param page 展开页的数据源或自定义页面
     * @throws IllegalArgumentException 已有展开页使用该标识
     *
     * @since Api 4
     */
    protected fun registerExpandedPage(id: String, page: ExploreExpandedPage) {
        require(id !in registeredExpandedPages)
        registeredExpandedPages[id] = page
    }
}
