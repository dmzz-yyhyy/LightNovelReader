package indi.dmzz_yyhyy.lightnovelreader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionManager
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionConverter
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionLoader
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionReadingService
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExampleExtension
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
        extensionLoader: ExtensionLoader
    ): RepositoryService = RepositoryService(repositoryRepository, extensionRepository, remoteDataSource, extensionLoader)

    @Provides
    @Singleton
    fun provideExtensionManager(): ExtensionManager = ExtensionManager()

    @Provides
    @Singleton
    fun provideExtensionConverter(): ExtensionConverter = ExtensionConverter()

    @Provides
    @Singleton
    fun provideExtensionLoader(
        @ApplicationContext context: Context
    ): ExtensionLoader = ExtensionLoader(context)

    @Provides
    @Singleton
    fun provideExampleExtension(): ExampleExtension = ExampleExtension()

    @Provides
    @Singleton
    fun provideExtensionReadingService(
        extensionManager: ExtensionManager,
        extensionConverter: ExtensionConverter,
        extensionLoader: ExtensionLoader
    ): ExtensionReadingService = ExtensionReadingService(extensionManager, extensionConverter, extensionLoader)
}
