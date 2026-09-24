# Benchmark 与 UI 自动化测试

该模块使用 AndroidX Macrobenchmark、AndroidJUnit4 和 UI Automator，从独立测试进程验证接近 release 配置的
`:app` benchmark 变体。测试由 Gradle instrumentation 直接运行，不依赖 PowerShell、Shell 或其他脚本控制应用。

## 测试分组

| 分组      | 测试类                             | 覆盖内容                                                     |
|---------|---------------------------------|----------------------------------------------------------|
| 性能      | `performance/StartupBenchmark`  | 冷启动、热启动及启动 trace                                         |
| 主导航     | `navigation/MainNavigationTest` | 四个根页面、统计概览、周/月/年统计详情                                     |
| 阅读与缓存管理 | `reading/ReadingAndManagerTest` | 继续阅读、最近阅读、下载、本地书籍、排序和缓存详情                                |
| 书架      | `bookshelf/BookshelfTest`       | 创建、重命名、重启持久化、空名称校验、排序、选择模式、移动、移除和删除确认                    |
| 探索与联网   | `explore/ExploreTest`           | 真实首页内容与书籍详情、搜索请求/响应、数据源标签页、展开页筛选/刷新/分页、搜索历史的创建、单项删除与全部清除 |
| 书籍与阅读器  | `book/BookAndReaderTest`        | 元数据、跨卷章节选择、EPUB 导出分支、批量已读、阅读器外观/控制/边距及设置持久化              |
| 设置      | `settings/SettingsTest`         | 插件、主题、排版规则 CRUD、繁体转换、语言与日期格式、更新、快照、存储、代理、日志、统计和许可证       |
| 插件系统    | `plugin/PluginSystemTest`       | 使用 PotatoLib 示例插件验证检查、取消安装、安装、启停、签名信息及卸载确认               |
| 后台任务    | `work/WorkManagerTest`          | 从应用 UI 触发完整数据导出/导入回环、书架导出、EPUB 导出、线上书籍缓存，以及主界面注册周期更新任务   |
| 系统入口    | `system/ExternalIntentTest`     | 插件安装与插件发现外部 Intent                                       |

当前共有 **52 项功能测试**和 **2 项启动性能测试**。

## 固定测试数据

`app/src/benchmark` 包含只进入 benchmark 变体的 fixture
receiver。每项功能测试开始前会清除应用数据、固定为英文、授予通知权限，并写入一本含两个卷和两个章节的本地书、阅读记录、统计记录及一个书架。receiver
只负责准备固定数据，不提供调度或直接运行 Worker 的测试入口。

本地数据存在时 benchmark 变体不会用远程结果覆盖 fixture，因此核心功能测试不依赖网络。receiver 不会进入
debug、snapshot 或 release 产物。

`benchmark/src/main/assets/PotatoLib.lnrp` 是固定的示例插件。测试 APK 通过 `FileProvider`
将其交给应用安装器，不依赖开发机绝对路径或预安装插件。

后台任务测试通过 UI Automator 操作应用和系统 DocumentsUI：导出测试校验目标文件非空；数据导入测试先从
UI 删除书架，再导入刚导出的快照并在重启后校验书架与书籍恢复；缓存测试从探索页真实搜索进入线上书籍，点击缓存按钮并等待界面显示完成；周期更新任务由主界面自动启动，测试将应用切到后台后同时校验
`CheckUpdateWork` 已执行成功且系统仍保留周期调度。测试不会从 receiver 或测试代码直接 enqueue Worker。

## 一次运行全部测试

确认 `adb devices` 中只有目标测试设备，然后在仓库根目录运行：

```powershell
.\gradlew.bat :app:lintVitalBenchmark :benchmark:connectedBenchmarkAndroidTest
```

只运行一个分组：

```powershell
.\gradlew.bat :benchmark:connectedBenchmarkAndroidTest `
  "-Pandroid.testInstrumentationRunnerArguments.class=indi.dmzz_yyhyy.lightnovelreader.benchmark.settings.SettingsTest"
```

只运行一个方法：

```powershell
.\gradlew.bat :benchmark:connectedBenchmarkAndroidTest `
  "-Pandroid.testInstrumentationRunnerArguments.class=indi.dmzz_yyhyy.lightnovelreader.benchmark.performance.StartupBenchmark#coldStartup"
```

`connectedBenchmarkAndroidTest` 构建 benchmark 应用时本身依赖 `lintVitalBenchmark`
；上面的完整命令仍将它显式列出，确保发布级 Lint 门禁不会因以后构建链变化而被遗漏。

联网测试会访问实际数据源。远端内容、限流策略或网络不可用都可能导致该分组失败；这类失败应与使用固定
fixture 的离线核心功能测试分开判断。

## 报告

- JUnit HTML：`benchmark/build/reports/androidTests/connected/benchmark/`
- 原始结果：`benchmark/build/outputs/androidTest-results/connected/benchmark/`
- Macrobenchmark 指标和 Perfetto trace：
  `benchmark/build/outputs/connected_android_test_additional_output/`

模拟器适合验证流程和采集 trace；发布门禁的性能阈值应在温控稳定的实体设备上建立。
