package indi.dmzz_yyhyy.lightnovelreader.data.web

import indi.dmzz_yyhyy.lightnovelreader.data.book.BookInformation
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookVolumes
import indi.dmzz_yyhyy.lightnovelreader.data.book.ChapterContent
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionBookIdManager
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
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A delegating WebBookDataSource that can switch implementations at runtime
 * This allows switching from fallback to extension once extensions are loaded
 */
@Singleton
class DelegatingWebDataSource @Inject constructor(
    private val extensionManager: ExtensionManager,
    private val extensionConverter: ExtensionConverter,
    private val userDataRepository: UserDataRepository,
    private val extensionBookIdManager: ExtensionBookIdManager
) : WebBookDataSource {

    private val webDataSources = listOf(ZaiComic, Wenku8Api)
    private val currentDataSource = AtomicReference<WebBookDataSource>(Wenku8Api)
    private var hasCheckedForExtension = false
    
    init {
        // Start with fallback, will switch when checkAndSwitchToPreferred is called
        resolveDataSource()
    }

    /**
     * Call this method when extensions are loaded to check if we should switch
     */
    fun checkAndSwitchToPreferred() {
        if (!hasCheckedForExtension) {
            hasCheckedForExtension = true
            resolveDataSource()
        }
    }

    private fun resolveDataSource() {
        try {
            val webDataSourcesId = userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path).get()
            println("DelegatingWebDataSource: Looking for preferred data source with ID: $webDataSourcesId")
            
            // Check built-in sources first
            val builtInDataSource = webDataSources.find { it.id == webDataSourcesId }
            if (builtInDataSource != null) {
                println("DelegatingWebDataSource: Using built-in data source: ${builtInDataSource.javaClass.simpleName}")
                currentDataSource.set(builtInDataSource)
                return
            }
            
            // Check extensions
            val extensionList = extensionManager.getAllExtensions()
            println("DelegatingWebDataSource: Found ${extensionList.size} extensions")
            
            if (extensionList.isNotEmpty()) {
                val extensionDataSources = extensionList.map { extension ->
                    ExtensionWebDataSourceAdapter(extension, extensionConverter, extensionBookIdManager)
                }
                
                val extensionDataSource = extensionDataSources.find { it.id == webDataSourcesId }
                if (extensionDataSource != null) {
                    println("DelegatingWebDataSource: Switching to extension data source: ${(extensionDataSource as ExtensionWebDataSourceAdapter).extensionName}")
                    currentDataSource.set(extensionDataSource)
                    return
                }
            }
            
            // Keep current fallback if nothing found
            println("DelegatingWebDataSource: Keeping fallback data source for ID: $webDataSourcesId")
            
        } catch (e: Exception) {
            println("DelegatingWebDataSource: Error resolving data source: ${e.message}")
        }
    }

    // Delegate all WebBookDataSource methods to the current implementation
    override val id: Int get() {
        val current = currentDataSource.get()
        println("DelegatingWebDataSource: Getting id from ${current::class.simpleName}: ${current.id}")
        return current.id
    }
    
    override suspend fun isOffLine(): Boolean {
        val current = currentDataSource.get()
        println("DelegatingWebDataSource: Checking isOffLine() from ${current::class.simpleName}")
        return current.isOffLine()
    }
    
    override val offLine: Boolean get() {
        val current = currentDataSource.get()
        println("DelegatingWebDataSource: Getting offLine from ${current::class.simpleName}: ${current.offLine}")
        return current.offLine
    }
    
    override val isOffLineFlow: Flow<Boolean> get() = currentDataSource.get().isOffLineFlow
    override val explorationPageIdList: List<String> get() {
        val current = currentDataSource.get()
        println("DelegatingWebDataSource: Getting explorationPageIdList from ${current::class.simpleName}: ${current.explorationPageIdList}")
        return current.explorationPageIdList
    }
    
    override val explorationPageDataSourceMap: Map<String, ExplorationPageDataSource> get() = currentDataSource.get().explorationPageDataSourceMap
    override val explorationExpandedPageDataSourceMap: Map<String, ExplorationExpandedPageDataSource> get() = currentDataSource.get().explorationExpandedPageDataSourceMap
    override val searchTypeMap: Map<String, String> get() = currentDataSource.get().searchTypeMap
    override val searchTipMap: Map<String, String> get() = currentDataSource.get().searchTipMap
    override val searchTypeIdList: List<String> get() = currentDataSource.get().searchTypeIdList
    
    override suspend fun getBookInformation(id: Int): BookInformation {
        val current = currentDataSource.get()
        println("DelegatingWebDataSource: Getting book information for ID $id from ${current::class.simpleName}")
        return current.getBookInformation(id)
    }
    
    override suspend fun getBookVolumes(id: Int): BookVolumes = currentDataSource.get().getBookVolumes(id)
    override suspend fun getChapterContent(chapterId: Int, bookId: Int): ChapterContent = currentDataSource.get().getChapterContent(chapterId, bookId)
    
    override fun search(searchType: String, keyword: String): Flow<List<BookInformation>> {
        val current = currentDataSource.get()
        println("DelegatingWebDataSource: Searching '$keyword' with type '$searchType' from ${current::class.simpleName}")
        return current.search(searchType, keyword)
    }
    
    override fun stopAllSearch() = currentDataSource.get().stopAllSearch()
}
