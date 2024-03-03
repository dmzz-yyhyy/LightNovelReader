package indi.dmzz_yyhyy.lightnovelreader.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import indi.dmzz_yyhyy.lightnovelreader.data.LightNovelReaderDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Singleton
    @Provides
    fun readingBookDao(db: LightNovelReaderDatabase) = db.readingBookDao()

    @Singleton
    @Provides
    fun bookMetadataDao(db: LightNovelReaderDatabase) = db.bookMetadataDao()

    @Singleton
    @Provides
    fun bookChapterListDao(db: LightNovelReaderDatabase) = db.bookChapterListDao()

    @Singleton
    @Provides
    fun bookChapterContentDao(db: LightNovelReaderDatabase) = db.bookChapterContentDao()
}