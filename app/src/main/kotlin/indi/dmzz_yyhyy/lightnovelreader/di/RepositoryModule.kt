package indi.dmzz_yyhyy.lightnovelreader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionManager
import indi.dmzz_yyhyy.lightnovelreader.data.extensions.ExtensionConverter
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
        remoteDataSource: indi.dmzz_yyhyy.lightnovelreader.data.repository.remote.RepositoryRemoteDataSource
    ): RepositoryService = RepositoryService(repositoryRepository, extensionRepository, remoteDataSource)

    @Provides
    @Singleton
    fun provideExtensionManager(): ExtensionManager = ExtensionManager()

    @Provides
    @Singleton
    fun provideExtensionConverter(): ExtensionConverter = ExtensionConverter()

    @Provides
    @Singleton
    fun provideExampleExtension(): ExampleExtension = ExampleExtension()
}
