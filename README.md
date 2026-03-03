**简体中文** | [繁體中文](README_TW.md) | [English](README_US.md) | [Русский](README_RU.md)

<div align="center">
    <h1>LightNovelReader</h1>
    <a><img alt="Android" src="https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white&style=for-the-badge"/></a>
    <a><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-0095D5.svg?logo=kotlin&logoColor=white&style=for-the-badge"/></a>
    <a><img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white&style=for-the-badge"></a>
    <a href="http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=P__gXIArh5UDBsEq7ttd4WhIYnNh3y1t&authKey=GAsRKEZ%2FwHpzRv19hNJsDnknOc86lYzNIHMPy2Jxt3S3U8f90qestOd760IAj%2F3l&noverify=0&group_code=867785526"><img alt="QQ Group" src="https://img.shields.io/badge/QQ讨论群-867785526-brightgreen.svg?logoColor=white&style=for-the-badge"></a>
    <a href="https://discord.gg/pnf4ABmDJt"><img alt="Discord" src="https://img.shields.io/badge/Discord-JOIN-4285F4.svg?logo=discord&logoColor=white&style=for-the-badge"></a>
    <a href="https://t.me/lightnoble"><img alt="Discord" src="https://img.shields.io/badge/Telegram-JOIN-188FCA.svg?logo=telegram&logoColor=white&style=for-the-badge"></a>
    <p>轻小说阅读软件，使用 Jetpack Compose 框架编写</p>
    <img src="assets/header.png" alt="drawing" width="80%"/>
</div>

## 介绍

LightNovelReader <sup>*重构版*</sup> 是一款开源的轻小说阅读软件，使用 Kotlin 和 Jetpack Compose 编写，具有轻量化的体积和流畅的阅读体验。此外，还有多种有用的功能，如 EPUB 导出、离线阅读和多数据源支持。

## 特性

- 完全重构的版本（可在[ 此处 ](https://github.com/dmzz-yyhyy/LightNovelReader/tree/master)查看重构前的分支）
- 使用 Jetpack Compose，提供流畅的阅读体验，支持 Android 7.0 ~ 15
- 缓存－支持缓存书本内容，以及离线优先的阅读
- 探索－发现新书、推荐榜，标签分类，关键词搜索……
- 多数据源支持－可以切换数据源，甚至可以看漫画。数据源之间数据独立
- 书架－完整的书架系统，支持创建和命名书架，将书本加入收藏、获取书本更新提示
- 将书本导出为 EPUB 文件
- 热情的开发者，还有更多…

## 插件开发与自定义数据源

您可以为LightNovelReader添加自定义的数据源与插件

您可以从[示例插件](https://github.com/dmzz-yyhyy/LightNovelReaderPlguin-Template)开始

欢迎各位开发者进行开发!

## 下载

从 [GitHub Releases](https://github.com/dmzz-yyhyy/LightNovelReader/releases/latest) 下载最新发布版。要体验最新的功能与 Bug 修复，请从 [Actions](https://github.com/dmzz-yyhyy/LightNovelReader/actions) 下载最新构建。

## 支持

- 在 [**此处**](https://github.com/dmzz-yyhyy/LightNovelReader/issues/new/choose) 提交一个 Bug 反馈或新功能请求
- 欢迎加入 QQ 讨论群：`867785526` | [**邀请链接**](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=P__gXIArh5UDBsEq7ttd4WhIYnNh3y1t&authKey=GAsRKEZ%2FwHpzRv19hNJsDnknOc86lYzNIHMPy2Jxt3S3U8f90qestOd760IAj%2F3l&noverify=0&group_code=867785526)
- 欢迎加入 Discord 服务器：[**邀请链接**](https://discord.gg/pnf4ABmDJt)
- 欢迎加入 Telegram 讨论群组：[**邀请链接**](https://t.me/lightnoble)

## 软件截图

|                             |
|-----------------------------|
| ![image](assets/light1.png) |
| ![image](assets/light2.png) |
| ![image](assets/light3.png) |

### 关于 EpubLib

为了处理epub的导出问题，我们单独创建了一个epub处理模块，如果您感兴趣，可以看[**这里**](https://github.com/dmzz-yyhyy/LightNovelReader/blob/refactoring/epub.md)

## 贡献

我们欢迎对 LightNovelReader 的贡献！以下是如何参与：

### 开始
1. Fork 本仓库。
2. 克隆你的 fork：`git clone https://github.com/your-username/LightNovelReader.git`
3. 为你的更改创建新分支：`git checkout -b feature/your-feature-name`
4. 进行更改并测试。
5. 按照下面的提交指南提交更改。
6. 推送到你的 fork：`git push origin feature/your-feature-name`
7. 向 `refactoring` 分支打开 Pull Request。

### 提交指南
- 保持提交原子化和描述性。
- 如果你的更改影响版本，请在 `app/build.gradle.kts` 中更新。

## 支持项目

[![爱发电赞助我们](https://img.shields.io/badge/❤%20支持我们-爱发电-orange)](https://www.ifdian.net/a/lightnovelreader)

LightNovelReader 是一个完全免费、开源的项目。  
如果你喜欢这个项目或它对你有所帮助，欢迎通过 [爱发电](https://www.ifdian.net/a/lightnovelreader) 支持我们。
所有款项将用于持续开发、新功能的实现、（如果有）服务器维护以及社区建设。

## License

```
Copyright (C) 2024 by NightFish <hk198580666@outlook.com>
Copyright (C) 2024 by yukonisen <yukonisen@curiousers.org>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
