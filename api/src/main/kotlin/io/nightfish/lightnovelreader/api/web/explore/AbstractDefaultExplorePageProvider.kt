package io.nightfish.lightnovelreader.api.web.explore

/**
 * [ExplorePageProvider.DefaultExplorePageProvider]的默认抽象实现
 * 提供了注册探索页面的便捷方法，子类需在初始化阶段调用这些方法完成页面的注册
 *
 * @since Api 2
 */
abstract class AbstractDefaultExplorePageProvider : ExplorePageProvider.DefaultExplorePageProvider {
    /** 已注册的探索页面 ID 有序列表 @since Api 2 */
    override val explorePageIdList = mutableListOf<String>()

    /** 以页面 ID 为键的探索卡片页数据源映射 @since Api 2 */
    override val exploreTapPageDataSourceMap = mutableMapOf<String, ExploreTapPageDataSource>()

    /** 以页面 ID 为键的探索展开页数据源映射 @since Api 2 */
    override val exploreExpandedPageDataSourceMap =
        mutableMapOf<String, ExploreExpandedPageDataSource>()

    private var index = 0

    /**
     * 注册一个指定id的探索卡片页
     *
     * @param id 页面的唯一标识
     * @param exploreTapPageDataSource 探索卡片页的数据源
     *
     * @since Api 2
     */
    protected fun registerTapPage(
        id: String,
        exploreTapPageDataSource: ExploreTapPageDataSource,
    ) {
        explorePageIdList.add(id)
        exploreTapPageDataSourceMap[id] = exploreTapPageDataSource
    }

    /**
     * 注册一个探索卡片页，使用自动递增id
     *
     * @param exploreTapPageDataSource 探索卡片页的数据源
     *
     * @since Api 2
     */
    protected fun registerTapPage(
        exploreTapPageDataSource: ExploreTapPageDataSource,
    ) {
        index++
        registerTapPage(index.toString(), exploreTapPageDataSource)
    }


    /**
     * 注册一个探索展开页的数据源
     *
     * @param id 页面的唯一标识
     * @param exploreExpandedPageDataSource 探索展开页的数据源
     *
     * @since Api 2
     */
    protected fun registerExpandedPageDataSource(
        id: String,
        exploreExpandedPageDataSource: ExploreExpandedPageDataSource
    ) {
        exploreExpandedPageDataSourceMap[id] = exploreExpandedPageDataSource
    }
}