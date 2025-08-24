package indi.dmzz_yyhyy.lightnovelreader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionBookIdManager
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionManager
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionConverter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionLoader
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionInitializer
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionReadingService
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExampleExtension
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.jar.JarExtensionLoader
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.lua.LuaExtensionParser
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.settings.ExtensionSettingsManager
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.LightNovelReaderDatabase
import indi.dmzz_yyhyy.lightnovelreader.data.repository.dao.ExtensionBookDao
import indi.dmzz_yyhyy.lightnovelreader.data.repository.dao.ExtensionSettingDao
import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryInitializer
import indi.dmzz_yyhyy.lightnovelreader.data.repository.ExtensionRepository
import indi.dmzz_yyhyy.lightnovelreader.data.repository.ExtensionRepositoryImpl
import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryRepository
import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryRepositoryImpl
import indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRepositoryRepository(
        impl: RepositoryRepositoryImpl
    ): RepositoryRepository = impl

    @Provides
    @Singleton
    fun provideExtensionRepository(
        impl: ExtensionRepositoryImpl
    ): ExtensionRepository = impl

    @Provides
    @Singleton
    fun provideRepositoryService(
        repositoryRepository: RepositoryRepository,
        extensionRepository: ExtensionRepository,
        remoteDataSource: indi.dmzz_yyhyy.lightnovelreader.data.repository.remote.RepositoryRemoteDataSource,
        extensionLoader: ExtensionLoader,
        extensionInitializer: ExtensionInitializer
    ): RepositoryService = RepositoryService(repositoryRepository, extensionRepository, remoteDataSource, extensionLoader, extensionInitializer)

    @Provides
    @Singleton
    fun provideExtensionManager(): ExtensionManager = ExtensionManager()

    @Provides
    @Singleton
    fun provideExtensionConverter(): ExtensionConverter = ExtensionConverter()

    @Provides
    @Singleton
    fun provideExtensionLoader(
        @ApplicationContext context: Context,
        luaExtensionParser: LuaExtensionParser,
        jarExtensionLoader: JarExtensionLoader
    ): ExtensionLoader = ExtensionLoader(context, luaExtensionParser, jarExtensionLoader)

    @Provides
    @Singleton
    fun provideLuaExtensionParser(): LuaExtensionParser = LuaExtensionParser()

    @Provides
    @Singleton
    fun provideJarExtensionLoader(): JarExtensionLoader = JarExtensionLoader()

    @Provides
    @Singleton
    fun provideExtensionSettingsManager(
        extensionSettingDao: ExtensionSettingDao
    ): ExtensionSettingsManager = ExtensionSettingsManager(extensionSettingDao)

    @Provides
    @Singleton
    fun provideExampleExtension(): ExampleExtension = ExampleExtension()

    @Provides
    @Singleton
    fun provideExtensionReadingService(
        extensionManager: ExtensionManager,
        extensionConverter: ExtensionConverter,
        extensionLoader: ExtensionLoader,
        extensionBookIdManager: ExtensionBookIdManager,
        extensionBookDao: ExtensionBookDao
    ): ExtensionReadingService = ExtensionReadingService(
        extensionManager, 
        extensionConverter, 
        extensionLoader,
        extensionBookIdManager,
        extensionBookDao
    )

    @Provides
    @Singleton
    fun provideExtensionBookIdManager(): ExtensionBookIdManager = ExtensionBookIdManager()

    @Provides
    @Singleton
    fun provideExtensionBookDao(database: LightNovelReaderDatabase): ExtensionBookDao = 
        database.extensionBookDao()

    @Provides
    @Singleton
    fun provideExtensionSettingDao(database: LightNovelReaderDatabase): ExtensionSettingDao = 
        database.extensionSettingDao()

    @Provides
    @Singleton
    fun provideExtensionInitializer(
        extensionLoader: ExtensionLoader,
        extensionManager: ExtensionManager,
        repositoryServiceProvider: javax.inject.Provider<indi.dmzz_yyhyy.lightnovelreader.data.repository.RepositoryService>
    ): ExtensionInitializer = ExtensionInitializer(extensionLoader, extensionManager, repositoryServiceProvider)

    @Provides
    @Singleton
    fun provideRepositoryInitializer(
        repositoryService: RepositoryService
    ): RepositoryInitializer = RepositoryInitializer(repositoryService)
}
