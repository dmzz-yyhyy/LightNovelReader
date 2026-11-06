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
class WebBookDataSourceManager @Inject constructor(
    val userDataRepository: UserDataRepository
) : WebBookDataSourceManagerApi {
    private val _webDataSourceItems = mutableListOf<WebDataSourceItem>()
    private val webDataSourceItemListMap = mutableMapOf<String, List<WebDataSourceItem>>()
    val webDataSourceItems: List<WebDataSourceItem> get() = _webDataSourceItems

    private val mutableWebDataSourceProvider = MutableWebDataSourceProvider()
    private val webBookDataSources = mutableListOf<WebBookDataSource>()
    private val loadedWebDataSourceIds = mutableSetOf<Identifier>()
    private var _enabledDataSourceIds: Set<Identifier> = emptySet()

    /** 用户启用、允许参与搜索/探索的数据源 */
    val enabledDataSourceIds: Set<Identifier> get() = _enabledDataSourceIds

    override fun registerWebDataSource(
        webBookDataSource: WebBookDataSource,
        webDataSourceItem: WebDataSourceItem
    ) {
        if (_webDataSourceItems.any { it.id == webDataSourceItem.id }) return
        _webDataSourceItems.add(webDataSourceItem)
        webBookDataSources.add(webBookDataSource)
        onWebDataSourceListChange()
    }

    override fun unregisterWebDataSource(webDataSourceId: Identifier) {
        _webDataSourceItems.removeAll { it.id == webDataSourceId }
        webBookDataSources.removeAll { it.id == webDataSourceId }
        loadedWebDataSourceIds.remove(webDataSourceId)
        onWebDataSourceListChange()
    }

    override fun getWebDataSource(): WebBookDataSource = mutableWebDataSourceProvider.value.origin

    fun loadWebDataSourcesFromClassLoader(
        classLoader: PathClassLoader,
        injector: PluginInjector,
        packageName: String,
        webDataSourceClassNames: List<String>
    ) {
        val items = mutableListOf<WebDataSourceItem>()
        webDataSourceClassNames.forEach { className ->
            val clazz =
                runCatching { classLoader.loadClass(className) }.getOrNull() ?: return@forEach
            if (!WebBookDataSource::class.java.isAssignableFrom(clazz)) return@forEach
            val instance = injector.provide<WebBookDataSource>(clazz)
            if (instance is WebBookDataSource) items.add(loadWebDataSourceClass(instance))
        }
        webDataSourceItemListMap[packageName] = items
    }

    fun <T : WebBookDataSource> loadWebDataSourceFromClass(
        clazz: Class<T>,
        injector: PluginInjector
    ) {
        if (!WebBookDataSource::class.java.isAssignableFrom(clazz)) return
        val instance = injector.provide<WebBookDataSource>(clazz)
        if (instance is WebBookDataSource) {
            val item = loadWebDataSourceClass(instance)
            val packageName = clazz.`package`?.name ?: return
            if (webDataSourceItemListMap.contains(packageName)) {
                webDataSourceItemListMap[packageName] =
                    webDataSourceItemListMap[packageName]!! + listOf(item)
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

    val primaryDataSourceId: Identifier get() = mutableWebDataSourceProvider.value.id

    fun setPrimaryWebDataSource(sourceId: Identifier) = runBlocking {
        userDataRepository
            .stringUserData(UserDataPath.Settings.Data.WebDataSourceId.path)
            .set(sourceId.toString())
        onWebDataSourceListChange()
    }

    fun onWebDataSourceListChange() = runBlocking {
        val configuredPrimaryId = userDataRepository
            .stringUserData(UserDataPath.Settings.Data.WebDataSourceId.path)
            .get()
            ?.convertOldId() ?: "Wenku8".ofId()

        // 未设置过启用列表时默认启用全部数据源
        val storedEnabled = userDataRepository
            .stringListUserData(UserDataPath.Settings.Data.EnabledWebDataSourceIds.path)
            .get()
        val enabledIds = storedEnabled
            ?.mapNotNull { runCatching { it.convertOldId() }.getOrNull() }
            ?.filter { id -> webBookDataSources.any { it.id == id } }
            ?.toSet()
            ?: webBookDataSources.map { it.id }.toSet()
        _enabledDataSourceIds = enabledIds

        // 主数据源必须处于启用状态
        val primaryId = if (configuredPrimaryId in enabledIds || enabledIds.isEmpty()) {
            configuredPrimaryId
        } else {
            enabledIds.first().also { fallback ->
                userDataRepository
                    .stringUserData(UserDataPath.Settings.Data.WebDataSourceId.path)
                    .set(fallback.toString())
            }
        }

        // 多活动数据源: 每个已注册的数据源都要初始化一次, 且只初始化一次
        webBookDataSources.forEach { dataSource ->
            if (loadedWebDataSourceIds.add(dataSource.id)) dataSource.onLoad()
        }
        mutableWebDataSourceProvider.updateAll(
            webBookDataSources.associateBy { it.id },
            primaryId,
            enabledIds
        )
    }

    /**
     * 启用/停用某个数据源。停用当前主数据源时会自动切换到另一个已启用数据源。
     */
    fun setWebDataSourceEnabled(sourceId: Identifier, enabled: Boolean) {
        val current = _enabledDataSourceIds.toMutableSet()
        if (enabled) current.add(sourceId) else current.remove(sourceId)
        runBlocking {
            userDataRepository
                .stringListUserData(UserDataPath.Settings.Data.EnabledWebDataSourceIds.path)
                .set(current.map { it.toString() })
            onWebDataSourceListChange()
        }
    }
}