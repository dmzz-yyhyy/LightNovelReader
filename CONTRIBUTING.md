**简体中文** | [繁體中文](CONTRIBUTING_TW.md) | [English](CONTRIBUTING_US.md)

# 为 LightNovelReader 做贡献

感谢你愿意为 LightNovelReader 做贡献！你可以通过报告问题、完善文档、改进翻译、修复缺陷或实现新功能参与项目。

提交代码前，请先阅读本文档。它说明了项目的分支、版本号、提交信息和 Pull Request（PR）规则。

## 开始之前

- 修复明确的小问题时，可以直接提交 PR。
- 对较大的功能、架构调整、插件 API 变更或界面重做，请先创建 Issue 或 Discussion，与维护者确认方案。
- 修复已有 Issue 时，请在 Issue 中留言，避免多人重复工作。
- Bug 报告应尽量包含应用版本、Android 版本、数据源、复现步骤和必要日志。
- 提交日志或截图前，请删除账号、令牌、书架内容等隐私信息。

项目地址：<https://github.com/dmzz-yyhyy/LightNovelReader>

## 开发环境

项目使用 Kotlin、Jetpack Compose 和 Gradle 构建。目前主要配置包括：

- JDK 21
- Gradle Wrapper（请使用仓库自带的 Wrapper）
- Android SDK，以 `app/build.gradle.kts` 中的配置为准
- IntelliJ IDEA 或 Android Studio

Fork 并克隆仓库后，建议添加上游仓库：

```bash
git remote add upstream https://github.com/dmzz-yyhyy/LightNovelReader.git
git fetch upstream
```

## 分支模型

长期分支的职责如下：

| 分支             | 用途                       | 是否接受普通 PR  |
|----------------|--------------------------|------------|
| `dev/<版本>`     | 下一个版本的日常开发；当前为 `dev/1.3` | 是          |
| `release/<版本>` | 已发布或即将发布版本的稳定与补丁维护       | 仅接受经过确认的修复 |
| `legacy`       | 保存旧版历史                   | 否          |

请选择正确的 PR 目标分支：

- 新功能、常规修复和重构：提交到当前 `dev/<版本>`。
- 需要进入旧版本补丁的修复：先与维护者确认，再提交到对应的 `release/<版本>`。
- 不要向 `legacy` 提交 PR。

开发分支必须短期存在，并从 PR 的目标分支创建：

```text
feat/<issue>-<description>
fix/<issue>-<description>
refactor/<description>
perf/<description>
docs/<description>
ci/<description>
chore/<description>
l10n/<locale>
hotfix/<version>-<description>
```

示例：

```text
feat/497-reader-typography
fix/485-wenku8-encoding
refactor/plugin-loader
hotfix/1.2.3-export-crash
```

分支名称使用小写英文，单词之间使用 `-`。有对应 Issue 时应包含 Issue 编号。

## 版本号规则

应用版本在 `app/build.gradle.kts` 中定义：

```kotlin
versionCode = x * 1_000_000 + y * 10_000 + z * 1_000 + build
versionName = "x.y.z"
```

例如：

```text
versionName = 1.3.0
versionCode = 1_03_00_009
```

最后三位是当前版本线的开发构建序号。

### 什么时候递增 `versionCode`

以下修改通常需要递增 `versionCode`：

- 修改 `app/src/main/kotlin/**` 中的应用行为。
- 修改会影响可安装 APK 的构建配置。
- 维护者明确要求生成新的可分发构建。

纯文档、CI 配置、商店元数据和自动本地化更新通常不需要修改版本号，除非维护者另有说明。

### 如何递增

`versionCode` 必须高于 PR **目标分支** 当前的值。

例如，`dev/1.3` 当前为：

```text
1_03_00_009
```

下一个合入该分支的代码 PR 应使用：

```text
1_03_00_010
```

提交到 `release/1.2.2` 的 PR 应与 `release/1.2.2` 比较，而不是与 `dev/1.3` 比较。

当多个 PR 并行开发时，可能出现多个 PR 使用同一构建序号的情况。合并前必须同步目标分支；如果该序号已被其他
PR 占用，请再次递增。不要仅为了通过检查而修改 `versionName`。

以下修改由维护者在准备新版本时完成：

- 修改 `versionName`。
- 修改主版本、次版本或补丁版本对应的 `versionCode` 位。
- 重置最后三位开发构建序号。
- 创建正式发布 Tag。

如果最后三位构建序号即将达到 `999`，请联系维护者，不要自行改变编码公式。

## 提交信息

提交信息采用 Conventional Commits 格式：

```text
<type>(<scope>): <简短说明>

[可选正文：解释修改原因、实现方式和兼容性影响]

[可选 footer：关联 Issue 或说明破坏性变更]
```

允许的主要类型：

| 类型         | 用途                     |
|------------|------------------------|
| `feat`     | 新功能                    |
| `fix`      | Bug 修复                 |
| `refactor` | 不改变外部行为的重构             |
| `perf`     | 性能优化                   |
| `docs`     | 文档                     |
| `test`     | 测试                     |
| `build`    | 构建、Gradle 或依赖          |
| `ci`       | GitHub Actions 等持续集成配置 |
| `chore`    | 其他维护工作                 |
| `l10n`     | 翻译和本地化                 |
| `revert`   | 回退已有修改                 |

推荐的 scope 包括：

```text
reader, bookshelf, source, api, plugin, epub,
proxy, compiler, ui, storage, deps, release
```

示例：

```text
feat(reader): 增加段落间距设置

fix(source-wenku8): 修复 GB18030 标点解码错误

refactor(plugin): 拆分插件加载与校验流程

build(deps): 移除不再使用的 Maven 仓库

l10n(zh-CN): 更新应用说明
```

存在不兼容变更时使用 `!`，并在 footer 中解释：

```text
feat(api)!: 调整插件路由返回类型

BREAKING CHANGE: 插件需要适配新的 Route.Book 接口。
```

提交说明可以使用中文或英文，但类型和 scope 使用小写英文。请避免 `temp`、`update`、`修复问题`
等无法表达具体内容的标题。

每个提交应尽量保持原子化，不要把无关格式化、依赖更新、重构和功能修改混在同一个提交中。

## 构建与测试

提交 PR 前，至少确认项目能够完成 Debug 构建。

Windows：

```powershell
.\gradlew.bat assembleDebug
```

macOS 或 Linux：

```bash
./gradlew assembleDebug
```


请为新增逻辑补充适当测试。界面修改应在 PR 中提供修改前后截图或录屏，并至少验证项目支持范围内的典型
Android 版本或设备。

## Pull Request

一个 PR 应只完成一项逻辑变更。尚未完成时请创建 Draft PR。

PR 标题使用与提交信息相同的格式，因为合并时会以 PR 标题作为 Squash Commit 标题：

```text
fix(epub): 修复仅导出所选分卷时包含多余章节
```

PR 描述至少应包含：

```markdown
## 修改内容

## 修改原因

## 验证方式

## 截图或录屏

close #<issue-number>
```

提交前请确认：

- PR 目标分支正确。
- 分支已同步目标分支的最新代码。
- 代码 PR 的 `versionCode` 高于目标分支。
- 构建、测试和 CI 检查通过。
- 没有夹带无关文件或大范围格式化。
- 新增界面文案已经考虑本地化。
- API 或插件兼容性变化已经明确说明。
- PR 已关联对应 Issue。

维护者可能要求修改实现、拆分 PR 或补充测试。PR 经审核通过后由维护者合并；合并后的开发分支可以删除。

## 翻译

项目使用 Crowdin 管理翻译。常规翻译修改应优先通过项目的 Crowdin 工作流完成，避免直接修改自动管理的翻译文件并产生冲突。

开发新增界面文案时，只在 `app/src/main/res/values/strings.xml` 中创建英文默认条目，并使用含义明确、可复用的资源名称。其他语言的翻译请通过 Crowdin 提交。
