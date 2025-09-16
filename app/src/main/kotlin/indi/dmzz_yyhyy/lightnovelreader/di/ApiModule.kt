package indi.dmzz_yyhyy.lightnovelreader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.Wenku8Api
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideWenku8Api(): Wenku8Api {
        return Wenku8Api
    }
}
