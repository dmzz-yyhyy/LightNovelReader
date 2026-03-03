package indi.dmzz_yyhyy.lightnovelreader.data.plugin

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import indi.dmzz_yyhyy.lightnovelreader.data.book.BookRepository
import indi.dmzz_yyhyy.lightnovelreader.data.bookshelf.BookshelfRepository
import indi.dmzz_yyhyy.lightnovelreader.data.content.ContentComponentRepository
import indi.dmzz_yyhyy.lightnovelreader.data.local.LocalBookDataSource
import indi.dmzz_yyhyy.lightnovelreader.data.local.room.dao.UserDataDao
import indi.dmzz_yyhyy.lightnovelreader.data.text.TextProcessingRepository
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.data.web.WebBookDataSourceManager
import io.nightfish.lightnovelreader.api.PluginContext
import io.nightfish.lightnovelreader.api.book.BookRepositoryApi
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfRepositoryApi
import io.nightfish.lightnovelreader.api.content.ContentComponentRepositoryApi
import io.nightfish.lightnovelreader.api.plugin.LightNovelReaderPlugin
import io.nightfish.lightnovelreader.api.text.TextProcessingRepositoryApi
import io.nightfish.lightnovelreader.api.userdata.UserDataDaoApi
import io.nightfish.lightnovelreader.api.userdata.UserDataRepositoryApi
import io.nightfish.lightnovelreader.api.web.WebBookDataSourceManagerApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginInjector @Inject constructor(
    pluginInjectorProvider: PluginInjectorProvider,
    @field:ApplicationContext appContext: Context,
    userDataDataDao: UserDataDao,
    userDataRepository: UserDataRepository,
    webBookDataSourceManager: WebBookDataSourceManager,
    textProcessingRepository: TextProcessingRepository,
    bookshelfRepository: BookshelfRepository,
    localBookDataSource: LocalBookDataSource,
    bookRepository: BookRepository,
    contentComponentRepository: ContentComponentRepository
) {
    val injectMap = mapOf(
        Context::class.java to appContext,
        UserDataDaoApi::class.java to userDataDataDao,
        UserDataRepositoryApi::class.java to userDataRepository,
        WebBookDataSourceManagerApi::class.java to webBookDataSourceManager,
        TextProcessingRepositoryApi::class.java to textProcessingRepository,
        LocalBookDataSourceApi::class.java to localBookDataSource,
        BookRepositoryApi::class.java to bookRepository,
        BookshelfRepositoryApi::class.java to bookshelfRepository,
        ContentComponentRepositoryApi::class.java to contentComponentRepository
    )

    init {
        pluginInjectorProvider.setPluginInjector(this)
    }

    fun <T>provide(clazz: Class<*>): T? = provide(clazz, injectMap)

    @Suppress("UNCHECKED_CAST")
    fun <T>provide(clazz: Class<*>, injectMap: Map<Class<*>, Any>): T? {
        try {
            return clazz.getDeclaredField("INSTANCE").get(null) as T
        }
        catch (_: NoSuchFieldException) { }
        catch (_: ClassCastException) { }
        try {
            return clazz.getDeclaredConstructor().newInstance() as T
        }
        catch (_: NoSuchMethodException) { }
        catch (_: SecurityException) { }
        try {
            clazz.constructors.forEach { constructor ->
                if (!constructor.parameterTypes.all { injectMap.keys.contains(it) }) return@forEach
                return constructor.newInstance(*constructor.parameterTypes.map { injectMap[it] }.toTypedArray()) as T
            }
        }
        catch (_: NoSuchFieldException) { }
        catch (_: ClassCastException) { }
        return null
    }

    fun providePlugin(clazz: Class<*>, pluginContext: PluginContext): LightNovelReaderPlugin? {
        return provide(clazz, injectMap.toMutableMap().apply {
            put(PluginContext::class.java, pluginContext)
        })
    }
}