package io.nightfish.lightnovelreader.api.web.explore

import io.nightfish.lightnovelreader.api.explore.ExploreBooksRow
import kotlinx.coroutines.flow.Flow

interface ExploreTapPageDataSource {
    val title: String
    fun getRowsFlow(): Flow<List<ExploreBooksRow>>
}