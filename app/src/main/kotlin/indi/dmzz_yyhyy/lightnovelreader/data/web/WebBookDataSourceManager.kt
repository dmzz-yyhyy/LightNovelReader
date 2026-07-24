package indi.dmzz_yyhyy.lightnovelreader.data.web

import dalvik.system.PathClassLoader
import indi.dmzz_yyhyy.lightnovelreader.data.plugin.injector.PluginInjector
import indi.dmzz_yyhyy.lightnovelreader.data.userdata.UserDataRepository
import indi.dmzz_yyhyy.lightnovelreader.utils.convertOldId
import indi.dmzz_yyhyy.lightnovelreader.utils.ofId
import io.nightfish.lightnovelreader.api.identifier.Identifier
import io.nightfish.lightnovelreader.api.userdata.UserDataPath
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebBookDataSourceManagerApi
import io.nightfish.lightnovelreader.api.web.WebDataSource
import io.nightfish.lightnovelreader.api.web.WebDataSourceItem
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class WebBookDataSourceManager @Inject constructor (
    val userDataRepository: UserDataRepository
): WebBookDataSourceManagerApi {
    private val _webDataSourceItems = mutableListOf<WebDataSourceItem>()
    private val webDataSourceItemListMap = mutableMapOf<String, List<WebDataSourceItem>>()
    val webDataSourceItems: List<WebDataSourceItem> get() = _webDataSourceItems

    private val mutableWebDataSourceProvider = MutableWebDataSourceProvider()
    private val webBookDataSources = mutableListOf<WebBookDataSource>()

    override fun registerWebDataSource(webBookDataSource: WebBookDataSource, webDataSourceItem: WebDataSourceItem) {
        if (_webDataSourceItems.any { it.id == webDataSourceItem.id }) return
        _webDataSourceItems.add(webDataSourceItem)
        webBookDataSources.add(webBookDataSource)
        onWebDataSourceListChange()
    }

    override fun unregisterWebDataSource(webDataSourceId: Identifier) {
        _webDataSourceItems.removeAll { it.id == webDataSourceId }
        webBookDataSources.removeAll { it.id == webDataSourceId }
        onWebDataSourceListChange()
    }

    override fun getWebDataSource(): WebBookDataSource = mutableWebDataSourceProvider.value.origin

    fun loadWebDataSourcesFromClassLoader(classLoader: PathClassLoader, injector: PluginInjector, packageName: String, webDataSourceClassNames: List<String>) {
        val items = mutableListOf<WebDataSourceItem>()
        webDataSourceClassNames.forEach { className ->
            val clazz = runCatching { classLoader.loadClass(className) }.getOrNull() ?: return@forEach
            if (!WebBookDataSource::class.java.isAssignableFrom(clazz)) return@forEach
            val instance = injector.provide<WebBookDataSource>(clazz)
            if (instance is WebBookDataSource) items.add(loadWebDataSourceClass(instance))
        }
        webDataSourceItemListMap[packageName] = items
    }

    fun <T: WebBookDataSource>loadWebDataSourceFromClass(clazz: Class<T>, injector: PluginInjector) {
        if (!WebBookDataSource::class.java.isAssignableFrom(clazz)) return
        val instance = injector.provide<WebBookDataSource>(clazz)
        if (instance is WebBookDataSource) {
            val item = loadWebDataSourceClass(instance)
            val packageName = clazz.`package`?.name ?: return
            if (webDataSourceItemListMap.contains(packageName)) {
                webDataSourceItemListMap[packageName] = webDataSourceItemListMap[packageName]!! + listOf(item)
            } else {
                webDataSourceItemListMap[packageName] = listOf(item)
            }
        }
    }

    fun loadWebDataSourceClass(instance: WebBookDataSource): WebDataSourceItem {
        val info = instance.javaClass.getAnnotationsByType(WebDataSource::class.java)
        val item = WebDataSourceItem(
            instance.id,
            info.first().name,
            info.first().provider,
        )
        registerWebDataSource(instance, item)
        return item
    }

    fun unloadWebDataSourcesFromClassLoader(packageName: String) {
        webDataSourceItemListMap[packageName]?.let { _webDataSourceItems.removeAll(it) }
    }

    fun getWebDataSourceProvider(): WebBookDataSourceProvider {
        return mutableWebDataSourceProvider
    }

    fun onWebDataSourceListChange() = runBlocking {
        val webDataSourcesId = userDataRepository
            .stringUserData(UserDataPath.Settings.Data.WebDataSourceId.path)
            .get()
            ?.convertOldId() ?: "Wenku8".ofId()
        mutableWebDataSourceProvider.update(
            webBookDataSources
                .find { it.id == webDataSourcesId }
                .also {
                    it?.onLoad()
                } ?: NotFoundWebDataSource(webDataSourcesId)
        )
    }
}