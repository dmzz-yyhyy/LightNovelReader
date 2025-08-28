package indi.dmzz_yyhyy.lightnovelreader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionBookIdManager
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
        return indi.dmzz_yyhyy.lightnovelreader.data.web.DelegatingWebDataSource(
            extensionManager,
            extensionConverter,
            userDataRepository
        )
    }

    @Singleton
    @Provides
    fun provideAllWebDataSources(
        extensionManager: ExtensionManager,
        extensionConverter: ExtensionConverter,
        extensionBookIdManager: ExtensionBookIdManager
    ): @JvmSuppressWildcards List<WebBookDataSource> {
        println("WebDataSourceModule: provideAllWebDataSources called")
        return try {
            // Get all extensions from ExtensionManager
            val extensionList = extensionManager.getAllExtensions()
            println("WebDataSourceModule: Found ${extensionList.size} extensions for all data sources")
            
            // Create WebBookDataSource instances for each extension
            val extensionDataSources = extensionList.map { extension ->
                ExtensionWebDataSourceAdapter(extension, extensionConverter, extensionBookIdManager)
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