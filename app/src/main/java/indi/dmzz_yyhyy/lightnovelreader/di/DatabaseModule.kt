package indi.dmzz_yyhyy.lightnovelreader.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import indi.dmzz_yyhyy.lightnovelreader.data.LightNovelReaderDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideLightNovelReaderDatabase(@ApplicationContext context: Context): LightNovelReaderDatabase =
        LightNovelReaderDatabase.getDB(context)
}