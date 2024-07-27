package indi.dmzz_yyhyy.lightnovelreader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.Wenku8Api
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object  WebDataSourceModule {
    @Singleton
    @Provides
    fun provideWebDataSource(): WebBookDataSource = Wenku8Api
}