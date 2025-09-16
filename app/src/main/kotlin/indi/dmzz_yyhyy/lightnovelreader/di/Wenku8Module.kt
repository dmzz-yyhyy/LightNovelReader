package indi.dmzz_yyhyy.lightnovelreader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.Wenku8CookieApplier
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.initWenku8CookieApplier
import indi.dmzz_yyhyy.lightnovelreader.data.web.wenku8.login.Wenku8SessionManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Wenku8Module {
    @Singleton
    @Provides
    fun provideWenku8CookieApplier(sessionManager: Wenku8SessionManager): Wenku8CookieApplier {
        val applier = Wenku8CookieApplier(sessionManager)
        initWenku8CookieApplier(applier)
        return applier
    }
}
