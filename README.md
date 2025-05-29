<div align="center">
       <h1>LightNovelReader</h1>
    <a><img alt="Android" src="https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white&style=for-the-badge"/></a>
    <a><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-0095D5.svg?logo=kotlin&logoColor=white&style=for-the-badge"/></a>
    <a><img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white&style=for-the-badge"></a>
    <a href="http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=P__gXIArh5UDBsEq7ttd4WhIYnNh3y1t&authKey=GAsRKEZ%2FwHpzRv19hNJsDnknOc86lYzNIHMPy2Jxt3S3U8f90qestOd760IAj%2F3l&noverify=0&group_code=867785526"><img alt="QQ Group" src="https://img.shields.io/badge/QQ讨论群-867785526-brightgreen.svg?logoColor=white&style=for-the-badge"></a>
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

## 下载
从 [GitHub Releases](https://github.com/dmzz-yyhyy/LightNovelReader/releases/latest) 下载最新发布版。要体验最新的功能与 Bug 修复，请从 [Actions](https://github.com/dmzz-yyhyy/LightNovelReader/actions) 下载最新构建。

## 支持

- 在 [**此处**](https://github.com/dmzz-yyhyy/LightNovelReader/issues/new/choose) 提交一个 Bug 反馈或新功能请求
- 欢迎加入 QQ 讨论群：`867785526` | [**邀请链接**](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=P__gXIArh5UDBsEq7ttd4WhIYnNh3y1t&authKey=GAsRKEZ%2FwHpzRv19hNJsDnknOc86lYzNIHMPy2Jxt3S3U8f90qestOd760IAj%2F3l&noverify=0&group_code=867785526)

## 软件截图

|                             |
|-----------------------------|
| ![image](assets/light1.png) |
| ![image](assets/light2.png) |
| ![image](assets/light3.png) |

### 关于 EpubLib
为了处理epub的导出问题，我们单独创建了一个epub处理模块，如果您感兴趣，可以看[**这里**](https://github.com/dmzz-yyhyy/LightNovelReader/blob/refactoring/epub.md)

## License

```
Copyright (C) 2024 by NightFish <hk198580666@outlook.com>
Copyright (C) 2024 by yukonisen <yukonisen@curiousers.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
```
