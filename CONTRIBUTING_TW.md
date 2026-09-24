[简体中文](CONTRIBUTING.md) | **繁體中文** | [English](CONTRIBUTING_US.md)

# 為 LightNovelReader 做出貢獻

感謝你願意為 LightNovelReader 做出貢獻！你可以透過回報問題、完善文件、改進翻譯、修正錯誤或實作新功能參與專案。

提交程式碼前，請先閱讀本文件。本文說明專案的分支、版本號、提交訊息和 Pull Request（PR）規則。

## 開始之前

- 修正明確的小問題時，可以直接提交 PR。
- 對於較大的功能、架構調整、外掛程式 API 變更或介面重做，請先建立 Issue 或 Discussion，與維護者確認方案。
- 修正現有 Issue 時，請在 Issue 中留言，避免多人重複處理。
- 錯誤回報應盡可能包含應用程式版本、Android 版本、資料來源、重現步驟和必要的記錄檔。
- 提交記錄檔或螢幕截圖前，請移除帳號、權杖、書架內容等隱私資訊。

專案位址：<https://github.com/dmzz-yyhyy/LightNovelReader>

## 開發環境

專案使用 Kotlin、Jetpack Compose 和 Gradle 建置。目前主要設定包括：

- JDK 21
- Gradle Wrapper（請使用儲存庫隨附的 Wrapper）
- Android SDK，以 `app/build.gradle.kts` 中的設定為準
- IntelliJ IDEA 或 Android Studio

Fork 並複製儲存庫後，建議新增上游儲存庫：

```bash
git remote add upstream https://github.com/dmzz-yyhyy/LightNovelReader.git
git fetch upstream
```

## 分支模型

長期分支的用途如下：

| 分支 | 用途 | 是否接受一般 PR |
|---|---|---|
| `dev/<版本>` | 下一個版本的日常開發；目前為 `dev/1.3` | 是 |
| `release/<版本>` | 已發布或即將發布版本的穩定與修補程式維護 | 僅接受經確認的修正 |
| `legacy` | 保存舊版歷史 | 否 |

請選擇正確的 PR 目標分支：

- 新功能、一般修正和重構：提交至目前的 `dev/<版本>`。
- 需要納入舊版本修補程式的修正：請先與維護者確認，再提交至對應的 `release/<版本>`。
- 請勿向 `legacy` 提交 PR。

開發分支必須是短期分支，並從 PR 的目標分支建立：

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

範例：

```text
feat/497-reader-typography
fix/485-wenku8-encoding
refactor/plugin-loader
hotfix/1.2.3-export-crash
```

分支名稱使用小寫英文，單字之間使用 `-`。有對應 Issue 時應包含 Issue 編號。

## 版本號規則

應用程式版本在 `app/build.gradle.kts` 中定義：

```kotlin
versionCode = x * 1_000_000 + y * 10_000 + z * 1_000 + build
versionName = "x.y.z"
```

例如：

```text
versionName = 1.3.0
versionCode = 1_03_00_009
```

最後三位數是目前版本線的開發建置序號。

### 何時遞增 `versionCode`

下列變更通常需要遞增 `versionCode`：

- 修改 `app/src/main/kotlin/**` 中的應用程式行為。
- 修改會影響可安裝 APK 的建置設定。
- 維護者明確要求產生新的可散佈建置版本。

純文件、CI 設定、商店中繼資料和自動在地化更新通常不需要修改版本號，除非維護者另有說明。

### 如何遞增

`versionCode` 必須高於 PR **目標分支**目前的值。

例如，`dev/1.3` 目前為：

```text
1_03_00_009
```

下一個合併至該分支的程式碼 PR 應使用：

```text
1_03_00_010
```

提交至 `release/1.2.2` 的 PR 應與 `release/1.2.2` 比較，而不是與 `dev/1.3` 比較。

當多個 PR 同時開發時，可能會有多個 PR 使用相同建置序號。合併前必須同步目標分支；如果該序號已由其他 PR 使用，請再次遞增。請勿只是為了通過檢查而修改 `versionName`。

下列變更由維護者在準備新版本時完成：

- 修改 `versionName`。
- 修改主版本、次版本或修訂版本所對應的 `versionCode` 位數。
- 重設最後三位開發建置序號。
- 建立正式發布 Tag。

如果最後三位建置序號即將達到 `999`，請聯絡維護者，請勿自行變更編碼公式。

## 提交訊息

提交訊息採用 Conventional Commits 格式：

```text
<type>(<scope>): <簡短說明>

[選填正文：說明修改原因、實作方式和相容性影響]

[選填 footer：關聯 Issue 或說明破壞性變更]
```

允許的主要類型：

| 類型 | 用途 |
|---|---|
| `feat` | 新功能 |
| `fix` | 錯誤修正 |
| `refactor` | 不改變外部行為的重構 |
| `perf` | 效能最佳化 |
| `docs` | 文件 |
| `test` | 測試 |
| `build` | 建置、Gradle 或相依套件 |
| `ci` | GitHub Actions 等持續整合設定 |
| `chore` | 其他維護工作 |
| `l10n` | 翻譯和在地化 |
| `revert` | 回復現有變更 |

建議的 scope 包括：

```text
reader, bookshelf, source, api, plugin, epub,
proxy, compiler, ui, storage, deps, release
```

範例：

```text
feat(reader): 新增段落間距設定

fix(source-wenku8): 修正 GB18030 標點符號解碼錯誤

refactor(plugin): 拆分外掛程式載入與驗證流程

build(deps): 移除不再使用的 Maven 儲存庫

l10n(zh-TW): 更新應用程式說明
```

有不相容變更時使用 `!`，並在 footer 中說明：

```text
feat(api)!: 調整外掛程式路由回傳型別

BREAKING CHANGE: 外掛程式需要適配新的 Route.Book 介面。
```

提交說明可以使用中文或英文，但類型和 scope 使用小寫英文。請避免 `temp`、`update`、`修正問題` 等無法表達具體內容的標題。

每個提交應盡可能保持單一目的，請勿將無關的格式化、相依套件更新、重構和功能修改混在同一個提交中。

## 建置與測試

提交 PR 前，至少確認專案能夠完成 Debug 建置。

Windows：

```powershell
.\gradlew.bat assembleDebug
```

macOS 或 Linux：

```bash
./gradlew assembleDebug
```

請為新增邏輯補充適當測試。介面修改應在 PR 中提供修改前後的螢幕截圖或錄影，並至少在專案支援範圍內的典型 Android 版本或裝置上進行驗證。

## Pull Request

一個 PR 應只完成一項邏輯變更。尚未完成時請建立 Draft PR。

PR 標題使用與提交訊息相同的格式，因為合併時會以 PR 標題作為 Squash Commit 標題：

```text
fix(epub): 修正僅匯出所選分卷時包含多餘章節
```

PR 說明至少應包含：

```markdown
## 修改內容

## 修改原因

## 驗證方式

## 螢幕截圖或錄影

close #<issue-number>
```

提交前請確認：

- PR 目標分支正確。
- 分支已同步目標分支的最新程式碼。
- 程式碼 PR 的 `versionCode` 高於目標分支。
- 建置、測試和 CI 檢查通過。
- 未夾帶無關檔案或大範圍格式化。
- 新增的介面文字已考慮在地化。
- API 或外掛程式相容性變更已明確說明。
- PR 已關聯對應的 Issue。

維護者可能會要求修改實作、拆分 PR 或補充測試。PR 通過審查後由維護者合併；合併後的開發分支可以刪除。

## 翻譯

專案使用 Crowdin 管理翻譯。一般翻譯修改應優先透過專案的 Crowdin 工作流程完成，避免直接修改自動管理的翻譯檔案而產生衝突。

開發新增介面文字時，只在 `app/src/main/res/values/strings.xml` 中建立英文預設項目，並使用含義明確、可重複使用的資源名稱。其他語言的翻譯請透過 Crowdin 提交。
