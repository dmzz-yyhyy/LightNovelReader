package indi.dmzz_yyhyy.lightnovelreader.data.explore

import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreRepository @Inject constructor(
    private val webBookDataSourceProvider: WebBookDataSourceProvider,
) {
    val searchProvider get() = webBookDataSourceProvider.value.searchProvider
    val explorePageProvider get() = webBookDataSourceProvider.value.explorePageProvider
}
