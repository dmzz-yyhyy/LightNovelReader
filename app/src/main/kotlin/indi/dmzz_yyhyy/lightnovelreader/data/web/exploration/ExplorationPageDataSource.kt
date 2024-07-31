package indi.dmzz_yyhyy.lightnovelreader.data.web.exploration

import indi.dmzz_yyhyy.lightnovelreader.data.exploration.ExplorationPage

interface ExplorationPageDataSource {
    fun getExplorationPage(): ExplorationPage
}