package indi.dmzz_yyhyy.lightnovelreader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionConverter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionManager
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionWebDataSourceAdapter
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataPath
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.Wenku8Api
import indi.dmzz_yyhyy.lightnovelreader.data.web.zaicomic.ZaiComic
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebDataSourceModule {
    private val webDataSources = listOf(ZaiComic, Wenku8Api)
    
    @Singleton
    @Provides
    fun provideWebDataSource(
        extensionManager: ExtensionManager,
        extensionConverter: ExtensionConverter,
        userDataRepository: UserDataRepository
    ): WebBookDataSource {
        val webDataSourcesId = userDataRepository.intUserData(UserDataPath.Settings.Data.WebDataSourceId.path).get()
        println("WebDataSourceModule: Looking for data source with ID: $webDataSourcesId")
        
        // First check built-in data sources
        webDataSources.forEach { source ->
            println("WebDataSourceModule: Built-in source: ${source.javaClass.simpleName} has ID: ${source.id}")
        }
        val builtInDataSource = webDataSources.find { it.id == webDataSourcesId }
        if (builtInDataSource != null) {
            println("WebDataSourceModule: Found built-in data source: ${builtInDataSource.javaClass.simpleName}")
            return builtInDataSource
        }
        
        // Then check extensions (only if built-in not found)
        try {
            val extensionList = extensionManager.getAllExtensions()
            println("WebDataSourceModule: Found ${extensionList.size} extensions")
            
            if (extensionList.isNotEmpty()) {
                val extensionDataSources = extensionList.map { extension ->
                    ExtensionWebDataSourceAdapter(extension, extensionConverter)
                }
                
                extensionDataSources.forEach { source ->
                    if (source is ExtensionWebDataSourceAdapter) {
                        println("WebDataSourceModule: Extension source: ${source.extensionName} has ID: ${source.id}")
                    }
                }
                
                val extensionDataSource = extensionDataSources.find { it.id == webDataSourcesId }
                if (extensionDataSource != null) {
                    println("WebDataSourceModule: Found extension data source: ${(extensionDataSource as ExtensionWebDataSourceAdapter).extensionName}")
                    return extensionDataSource
                }
            } else {
                println("WebDataSourceModule: Extensions not loaded yet, will fall back to Wenku8Api temporarily")
                // Don't reset the user preference - just fall back temporarily
                // The user's preference (webDataSourcesId) remains in UserData
                // Once extensions load, the app should be able to find the right extension
            }
        } catch (e: Exception) {
            // If extension loading fails, fall back to default
            println("WebDataSourceModule: Failed to load extensions, falling back to Wenku8Api: ${e.message}")
        }
        
        // Fall back to default if nothing found
        println("WebDataSourceModule: No matching data source found for ID $webDataSourcesId, falling back to Wenku8Api")
        return Wenku8Api
    }

    @Singleton
    @Provides
    fun provideAllWebDataSources(
        extensionManager: ExtensionManager,
        extensionConverter: ExtensionConverter
    ): @JvmSuppressWildcards List<WebBookDataSource> {
        println("WebDataSourceModule: provideAllWebDataSources called")
        return try {
            // Get all extensions from ExtensionManager
            val extensionList = extensionManager.getAllExtensions()
            println("WebDataSourceModule: Found ${extensionList.size} extensions for all data sources")
            
            // Create WebBookDataSource instances for each extension
            val extensionDataSources = extensionList.map { extension ->
                ExtensionWebDataSourceAdapter(extension, extensionConverter)
            }
            
            extensionDataSources.forEach { source ->
                if (source is ExtensionWebDataSourceAdapter) {
                    println("WebDataSourceModule: Extension data source: ${source.extensionName} ID: ${source.id}")
                }
            }
            
            // Return all data sources (built-in + extensions)
            val allSources = webDataSources + extensionDataSources
            println("WebDataSourceModule: Returning ${allSources.size} total data sources (${webDataSources.size} built-in + ${extensionDataSources.size} extensions)")
            allSources
        } catch (e: Exception) {
            // If extension loading fails, return only built-in sources
            println("WebDataSourceModule: Failed to load extensions for all data sources, returning built-in only: ${e.message}")
            e.printStackTrace()
            webDataSources
        }
    }
}