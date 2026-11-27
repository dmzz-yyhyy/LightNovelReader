package io.nightfish.lightnovelreader.api.web.explore

/** 提供默认探索界面的页面注册方法。 */
abstract class AbstractDefaultExplorePageProvider : ExplorePageProvider.DefaultExplorePageProvider {
    private val registeredTapPages = linkedMapOf<String, ExploreTapPage>()
    private val registeredExpandedPages = mutableMapOf<String, ExploreExpandedPage>()
    private var nextTapId = 0

    override val exploreTapPages: Map<String, ExploreTapPage> get() = registeredTapPages.toMap()
    override val exploreExpandedPages: Map<String, ExploreExpandedPage> get() = registeredExpandedPages.toMap()

    /**
     * 注册默认标签页或自定义标签页。
     *
     * @param id 唯一的页面标识
     * @param page 注册的页面
     */
    protected fun registerTapPage(id: String, page: ExploreTapPage) {
        require(id !in registeredTapPages)
        registeredTapPages[id] = page
    }

    /**
     * 使用自动分配的标识注册标签页。
     *
     * @param page 注册的页面
     */
    protected fun registerTapPage(page: ExploreTapPage) {
        var id: String
        do {
            id = (++nextTapId).toString()
        } while (id in registeredTapPages)
        registerTapPage(id, page)
    }

    /**
     * 注册默认展开页或自定义展开页。
     *
     * @param id 唯一的路由标识
     * @param page 注册的页面
     */
    protected fun registerExpandedPage(id: String, page: ExploreExpandedPage) {
        require(id !in registeredExpandedPages)
        registeredExpandedPages[id] = page
    }
}
