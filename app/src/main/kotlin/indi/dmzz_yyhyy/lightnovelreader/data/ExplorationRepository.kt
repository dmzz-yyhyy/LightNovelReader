package indi.dmzz_yyhyy.lightnovelreader.data

import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExplorationRepository @Inject constructor(
    private val webBookDataSource: WebBookDataSource
) {
    suspend fun getExplorationPageMap() = webBookDataSource.getExplorationPageMap()
    suspend fun getExplorationPageTitleList() = webBookDataSource.getExplorationPageTitleList()
}