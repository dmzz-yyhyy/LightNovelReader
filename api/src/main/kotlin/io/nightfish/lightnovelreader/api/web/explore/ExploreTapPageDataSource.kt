package io.nightfish.lightnovelreader.api.web.explore

import io.nightfish.lightnovelreader.api.explore.ExploreBooksRow
import kotlinx.coroutines.flow.Flow

/**
 * 探索卡片页的数据源接口
 * 卡片页以行为单位展示书本信息
 *
 * @since Api 2
 */
interface ExploreTapPageDataSource : ExploreTapPage {
    /**
     * 获取卡片页行列表的数据流
     *
     * @return 探索页行列表的数据流
     *
     * @since Api 2
     */
    fun getRowsFlow(): Flow<List<ExploreBooksRow>>
}