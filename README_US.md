[简体中文](README.md) | [繁體中文](README_TW.md) | **English** | [Русский](README_RU.md)

<div align="center">
    <h1>LightNovelReader</h1>
    <a><img alt="Android" src="https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white&style=for-the-badge"/></a>
    <a><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-0095D5.svg?logo=kotlin&logoColor=white&style=for-the-badge"/></a>
    <a><img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white&style=for-the-badge"></a>
    <a href="http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=P__gXIArh5UDBsEq7ttd4WhIYnNh3y1t&authKey=GAsRKEZ%2FwHpzRv19hNJsDnknOc86lYzNIHMPy2Jxt3S3U8f90qestOd760IAj%2F3l&noverify=0&group_code=867785526"><img alt="QQ Group" src="https://img.shields.io/badge/QQ讨论群-867785526-brightgreen.svg?logoColor=white&style=for-the-badge"></a>
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

## Download

Download the latest release from [GitHub Releases](https://github.com/dmzz-yyhyy/LightNovelReader/releases/latest). To experience the latest features and bug fixes, download the latest build from [Actions](https://github.com/dmzz-yyhyy/LightNovelReader/actions).

## Support

- Found a bug or have a feature idea? Submit it [**here**](https://github.com/dmzz-yyhyy/LightNovelReader/issues/new/choose)
- Join the QQ discussion group: `867785526` | [**Invitation Link**](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=P__gXIArh5UDBsEq7ttd4WhIYnNh3y1t&authKey=GAsRKEZ%2FwHpzRv19hNJsDnknOc86lYzNIHMPy2Jxt3S3U8f90qestOd760IAj%2F3l&noverify=0&group_code=867785526)

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
- Use conventional commit format: `<type>(<scope>): <description>`
  - Types: `feat` (new feature), `fix` (bug fix), `docs` (documentation), `style` (formatting), `refactor` (code restructuring), `test` (testing), `chore` (maintenance)
  - Example: `feat: add dark mode support`
- Keep commits atomic and descriptive.
- If your change affects the version, update it in `app/build.gradle.kts`.

### Version Management
Versions are managed in `app/build.gradle.kts`:
- `versionNameStr`: The public version (e.g., "1.3.1"). Follow semantic versioning (major.minor.patch).
- `debugNumber`: Increment for development builds (0 for releases).
- `versionCode`: Auto-calculated as major*1000000 + minor*10000 + patch*1000 + debugNumber.

For releases, update `versionNameStr` and reset `debugNumber` to 0.

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
