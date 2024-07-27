package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration

import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationPage
import kotlinx.coroutines.flow.Flow

interface ExplorationPageDataSource {
    fun getExplorationPage(): Flow<ExplorationPage>
}