package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionConverter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionManager
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionWebDataSourceAdapter
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationExpandedPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.exploration.ExplorationPageDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.ZaiComic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A reactive data source that can switch between built-in and extension sources
 * automatically when extensions become available
 */
class ReactiveWebDataSource(
    private val extensionManager: ExtensionManager,
    private val extensionConverter: ExtensionConverter,
    private val userDataRepository: UserDataRepository
) : WebBookDataSource {

    private val builtInSources = listOf(ZaiComic, Wenku8Api)
    private var currentDataSource: WebBookDataSource = Wenku8Api
    
    init {
        updateCurrentDataSource()
    }

    private fun updateCurrentDataSource() {
        val preferredSourceId = userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path).get()
        println("ReactiveWebDataSource: Looking for preferred source ID: $preferredSourceId")
        
        // First check built-in sources
        val builtInSource = builtInSources.find { it.id == preferredSourceId }
        if (builtInSource != null) {
            println("ReactiveWebDataSource: Using built-in source: ${builtInSource.javaClass.simpleName}")
            currentDataSource = builtInSource
            return
        }
        
        // Then check extensions
        val extensions = extensionManager.getAllExtensions()
        println("ReactiveWebDataSource: Found ${extensions.size} extensions")
        
        for (extension in extensions) {
            val adapter = ExtensionWebDataSourceAdapter(extension, extensionConverter)
            if (adapter.id == preferredSourceId) {
                println("ReactiveWebDataSource: Using extension source: ${extension.name}")
                currentDataSource = adapter
                return
            }
        }
        
        // Fall back to default
        println("ReactiveWebDataSource: Preferred source not found, using Wenku8Api as fallback")
        currentDataSource = Wenku8Api
    }
    
    /**
     * Call this when extensions are loaded to potentially switch to the user's preferred extension
     */
    fun onExtensionsLoaded() {
        updateCurrentDataSource()
    }

    override val id: Int get() = currentDataSource.id
    override val offLine: Boolean get() = currentDataSource.offLine
    override val isOffLineFlow: Flow<Boolean> get() = currentDataSource.isOffLineFlow
    override val explorationPageIdList: List<String> get() = currentDataSource.explorationPageIdList
    override val explorationPageDataSourceMap: Map<String, ExplorationPageDataSource> get() = currentDataSource.explorationPageDataSourceMap
    override val explorationExpandedPageDataSourceMap: Map<String, ExplorationExpandedPageDataSource> get() = currentDataSource.explorationExpandedPageDataSourceMap
    override val searchTypeMap: Map<String, String> get() = currentDataSource.searchTypeMap
    override val searchTipMap: Map<String, String> get() = currentDataSource.searchTipMap
    override val searchTypeIdList: List<String> get() = currentDataSource.searchTypeIdList

    override suspend fun isOffLine(): Boolean = currentDataSource.isOffLine()
    override suspend fun getBookInformation(id: Int): BookInformation = currentDataSource.getBookInformation(id)
    override suspend fun getBookVolumes(id: Int): BookVolumes = currentDataSource.getBookVolumes(id)
    override suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent = currentDataSource.getChapterContent(chapterId, bookId)
    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> = currentDataSource.search(searchType, keyword)
    override fun stopAllSearch() = currentDataSource.stopAllSearch()
}
