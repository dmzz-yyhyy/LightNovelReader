package indi.dmzz_yyhyy.lightnovelreader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class) // 这里使用了 SingletonComponent 作为示例，你可以根据实际情况选择不同的组件
object CoroutineDispatcherModule {
    @Provides
    fun provideCoroutineDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO // 在这里提供你需要的 CoroutineDispatcher 对象，这里使用 Dispatchers.IO 作为示例
    }
}