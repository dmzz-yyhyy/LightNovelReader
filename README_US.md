[简体中文](README.md) | [繁體中文](README_TW.md) | **English** | [Русский](README_RU.md)

<div align="center">
    <h1>LightNovelReader</h1>
    <a><img alt="Android" src="https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white&style=for-the-badge"/></a>
    <a><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-0095D5.svg?logo=kotlin&logoColor=white&style=for-the-badge"/></a>
    <a><img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white&style=for-the-badge"></a>
    <a href="http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=P__gXIArh5UDBsEq7ttd4WhIYnNh3y1t&authKey=GAsRKEZ%2FwHpzRv19hNJsDnknOc86lYzNIHMPy2Jxt3S3U8f90qestOd760IAj%2F3l&noverify=0&group_code=867785526"><img alt="QQ Group" src="https://img.shields.io/badge/QQ讨论群-867785526-brightgreen.svg?logoColor=white&style=for-the-badge"></a>
    <a href="https://discord.gg/pnf4ABmDJt"><img alt="Discord" src="https://img.shields.io/badge/Discord-JOIN-4285F4.svg?logo=discord&logoColor=white&style=for-the-badge"></a>
    <a href="https://t.me/lightnoble"><img alt="Discord" src="https://img.shields.io/badge/Telegram-JOIN-188FCA.svg?logo=telegram&logoColor=white&style=for-the-badge"></a>
    <p>Light novel reading application built with Jetpack Compose</p>
    <img src="assets/header.png" alt="drawing" width="80%"/>
</div>

## Introduction

LightNovelReader <sup>*Refactored Version*</sup> is an open-source app for reading light novels, built with Kotlin and Jetpack Compose. It’s designed for a smooth, modern reading experience and packed with useful features like EPUB exports, offline reading, and support for multiple data sources.

## Features

- Fully refactored version (see [pre-refactoring branch](https://github.com/dmzz-yyhyy/LightNovelReader/tree/master))
- Modern UI with Jetpack Compose, compatible with Android 7.0 through 15
- Caching - support for caching book content and offline-first reading
- Explore - discover new books, recommendation lists, tag categories, keyword search...
- Multi-source support - easily switch between data sources, including manga. Data is independent between sources
- Bookshelf - bookshelf management with custom shelves, favorites, and update notifications
- EPUB export functionality for your favorite novels
- Active development with passionate contributors

## Plugin Development and Custom Data Sources

You can add custom data sources and plugins to LightNovelReader.

You can start with the [sample plugin](https://github.com/dmzz-yyhyy/LightNovelReaderPlugin-Template)

Developers are welcome to contribute!

## Download

Download the latest release from [GitHub Releases](https://github.com/dmzz-yyhyy/LightNovelReader/releases/latest). To experience the latest features and bug fixes, download the latest build from [Actions](https://github.com/dmzz-yyhyy/LightNovelReader/actions).

## Support

- Found a bug or have a feature idea? Submit it [**here**](https://github.com/dmzz-yyhyy/LightNovelReader/issues/new/choose)
- Join the QQ discussion group: `867785526` | [**Invitation Link**](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=P__gXIArh5UDBsEq7ttd4WhIYnNh3y1t&authKey=GAsRKEZ%2FwHpzRv19hNJsDnknOc86lYzNIHMPy2Jxt3S3U8f90qestOd760IAj%2F3l&noverify=0&group_code=867785526)
- Join our Discord server: [**Invitation Link**](https://discord.gg/pnf4ABmDJt)
- Join our Telegram group: [**Invitation Link**](https://t.me/lightnoble)

## Screenshots

|                             |
|-----------------------------|
| ![image](assets/light1.png) |
| ![image](assets/light2.png) |
| ![image](assets/light3.png) |

### About EpubLib

We’ve developed a dedicated module to handle EPUB export more effectively. If you're interested, check it out [**here**](https://github.com/dmzz-yyhyy/LightNovelReader/blob/refactoring/epub.md)

## Contributing

We welcome contributions to LightNovelReader! Here's how you can get involved:

### Getting Started
1. Fork the repository.
2. Clone your fork: `git clone https://github.com/your-username/LightNovelReader.git`
3. Create a new branch for your changes: `git checkout -b feature/your-feature-name`
4. Make your changes and test them.
5. Commit your changes following the commit guidelines below.
6. Push to your fork: `git push origin feature/your-feature-name`
7. Open a Pull Request to the `refactoring` branch.

### Commit Guidelines
- Keep commits atomic and descriptive.
- If your change affects the version, update it in `app/build.gradle.kts`.

## Support the Project

[![Support Us on Aifadian](https://img.shields.io/badge/❤%20Support%20Us-ifdian-orange)](https://www.ifdian.net/a/lightnovelreader)

LightNovelReader is a fully free and open-source project.  
If you enjoy using it or find it helpful, consider supporting us through [Aifadian](https://www.ifdian.net/a/lightnovelreader) (a China-based platform similar to Patreon).  
All contributions go toward continuous development, new features, possible future server maintenance, and community growth.
Your support helps keep the project alive and makes reading even better for everyone.


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
