# 多活动数据源与书架聚合（实验性）

## 背景

原实现里同一时刻只有一个「活动数据源」：

- `MutableWebDataSourceProvider` 只保存一个 `_value`，所有书籍查询都走它；
- Room 表没有数据源维度，切换数据源时需要导出当前数据、清空数据库、再导入目标源的数据；
- 清库/导入期间所有单例、Flow、Paging、下载任务都持有旧状态，所以只能重启应用收尾。

这个改动尝试把「数据源」拆成两层：

- **主数据源（primary）**：探索、搜索、热门榜等没有书籍上下文的入口仍然只使用它；
- **活动数据源集合（active sources）**：所有已注册的数据源都可以被按书籍寻址，书架可以同时展示来自多个源的书。

## 实现要点

### 1. provider 支持多个数据源

`WebBookDataSourceProvider` 新增：

```kotlin
fun get(id: Identifier): ProxyWebBookDataSource?
fun getAll(): List<ProxyWebBookDataSource>
```

`MutableWebDataSourceProvider.updateAll()` 会为每个数据源建立独立的代理链（Cached → Coalescing → Priority），
`value` 仍然表示主数据源，保持旧调用点不变。

`WebBookDataSourceManager.onWebDataSourceListChange()` 会初始化所有已注册数据源（每个只 `onLoad()` 一次），
并根据用户设置确定主数据源。

### 2. 书籍 → 数据源归属

`book_information` 新增可空列 `web_book_data_source_id`（数据库版本 18 → 19，迁移只加列）。
书籍信息第一次从某个源写入时记录来源；旧数据没有来源时回退到主数据源。

`BookSourceResolver` 统一按 `bookId` 解析数据源：

```kotlin
val source = bookSourceResolver.resolve(bookId)
source.getBookVolumes(bookId, priority)
```

`BookRepository`、缓存 Worker、更新检查 Worker、EPUB 导出都改为按书解析，而不是固定使用主源。

### 3. 切换数据源不再清库、不再重启

`SourceChangeViewModel.changeWebSource()` 现在只做三件事：

1. 把目标数据源的历史存档（`local_data/<sourceId>`，若存在）**合并**进当前数据库；
2. 写入 `WebDataSourceId` 设置；
3. 调用 `WebBookDataSourceManager.onWebDataSourceListChange()` 立即切换主源。

`LocalDataManager.importLocalDataToDatabase()` 对书架实体做并集合并（同一书架 id 下合并书单），
因此不会因为目标源的书架快照覆盖已有书架。

### 4. 书架聚合

- 切换时不再删除其他数据源的数据，所有源的书架条目、书架元数据、书籍信息可以共存；
- 升级后的首次启动会把所有历史存档中的「书架相关数据」合并一次（书架、书架元数据、书架书籍信息），
  无需用户逐个切换数据源；
- 书架 UI 本来就是按 `bookId` 组装书籍卡片，配合上面的按书寻址即可直接展示多源书籍。

### 5. 跨源同 id 不再覆盖

新数据的本地书 id 形如 `数据源id|原始书id`（`SourceBookId.kt`）：
- 搜索、探索、展开页拿到的书 id 在进入本地前都会用所属数据源限定；
- `BookSourceResolver.resolveBook()` 负责把本地 id 拆成 `(数据源, 远端书 id)`，
  仓储、缓存、更新检查、EPUB 导出对数据源只用远端 id；
- 旧数据没有前缀，仍按原始 id + 书籍信息里的数据源归属处理。

### 6. 搜索 / 探索作用于全部数据源

- 搜索与搜索建议会并发查询所有已启用数据源，结果统一限定书 id 后合并返回；
- 探索页顶部新增数据源筛选条，可切换查看任意已启用数据源的探索页与展开页。

### 7. 数据源与插件合并到一个设置页

- 设置里的「数据源」「插件」合并为「数据源与插件」；
- 页面顶部列出所有已注册数据源，可直接启用/停用并选择主数据源；
- 下方保留插件列表、启用开关、安装与更新入口；
- 启用列表持久化在 `Settings.Data.EnabledWebDataSourceIds`，停用当前主源会自动切换主源。

## 已知限制

- 章节 / 卷 id 仍然只按远端 id 存储：两个数据源如果生成完全相同的章节 id，章节内容缓存仍可能冲突（书籍层面的冲突已通过限定书 id 解决）。
- 探索页采用「选择数据源」而不是把多个源的探索页同时铺开。
- 封面和正文图片的 HTTP header 仍取主数据源；多数源的图片不需要特殊 header，但并非所有源都如此。
- 备份格式（`LocalData`）仍然只有单个 `webBookDataSourceId` 字段；跨源备份恢复时，无归属信息的旧书会回退到主源。
- 只在本地做了编译验证，没有在真机上跑过完整的双源阅读流程。

## 手动验证建议

1. 安装两个数据源插件（例如 Wenku8 与任意第三方源）；
2. 在主源 A 中收藏一本书，进入阅读；
3. 切换到源 B（此时不应清空书架、不应重启），在 B 中再收藏一本书；
4. 回到书架：应同时看到 A、B 两本书，且都能打开阅读；
5. 在 A 中打开 B 的书、缓存章节、导出 EPUB，确认请求发往 B 而不是 A。
