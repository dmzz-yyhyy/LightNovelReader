[简体中文](README.md) | [繁體中文](README_TW.md) | [English](README_US.md) | **Русский**

<div align="center">
    <h1>LightNovelReader</h1>
    <a><img alt="Android" src="https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white&style=for-the-badge"/></a>
    <a><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-0095D5.svg?logo=kotlin&logoColor=white&style=for-the-badge"/></a>
    <a><img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white&style=for-the-badge"></a>
    <a href="http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=P__gXIArh5UDBsEq7ttd4WhIYnNh3y1t&authKey=GAsRKEZ%2FwHpzRv19hNJsDnknOc86lYzNIHMPy2Jxt3S3U8f90qestOd760IAj%2F3l&noverify=0&group_code=867785526"><img alt="QQ Group" src="https://img.shields.io/badge/QQ讨论群-867785526-brightgreen.svg?logoColor=white&style=for-the-badge"></a>
    <a href="https://discord.gg/pnf4ABmDJt"><img alt="Discord" src="https://img.shields.io/badge/Discord-JOIN-4285F4.svg?logo=discord&logoColor=white&style=for-the-badge"></a>
    <a href="https://t.me/lightnoble"><img alt="Discord" src="https://img.shields.io/badge/Telegram-JOIN-188FCA.svg?logo=telegram&logoColor=white&style=for-the-badge"></a>
    <p>Приложение для чтения ранобэ, созданное с использованием Jetpack Compose</p>
    <img src="assets/header.png" alt="drawing" width="80%"/>
</div>

## Введение

LightNovelReader <sup>*Переработанная версия*</sup> — это приложение с открытым исходным кодом для чтения ранобэ, разработанное на Kotlin и Jetpack Compose. Оно создано для плавного и современного чтения с множеством полезных функций, таких как экспорт в EPUB, офлайн-чтение и поддержка нескольких источников данных.

## Особенности

- Полностью переработанная версия (см. [ветку до рефакторинга](https://github.com/dmzz-yyhyy/LightNovelReader/tree/master))
- Современный интерфейс на Jetpack Compose, поддержка Android 7.0—15
- Кэширование — поддержка кэширования содержимого книг и чтения в офлайн-режиме
- Обзор — открывайте новые книги, списки рекомендаций, категории тегов, поиск по ключевым словам и многое другое...
- Поддержка множества источников — легко переключайтесь между источниками, включая мангу. Данные между источниками независимы
- Полка — управление книжной полкой с пользовательскими полками, избранным и уведомлениями об обновлениях
- Экспорт книг в формат EPUB
- Активная разработка с увлечёнными участниками

## Загрузка

Скачайте последнюю версию в [релизах GitHub](https://github.com/dmzz-yyhyy/LightNovelReader/releases/latest). Чтобы получить последние функции и исправления, скачайте свежую сборку из [Actions](https://github.com/dmzz-yyhyy/LightNovelReader/actions).

## Поддержка

- Нашли ошибку или есть идеи? Сообщите [**здесь**](https://github.com/dmzz-yyhyy/LightNovelReader/issues/new/choose)
- Присоединяйтесь к группе обсуждений QQ: `867785526` | [**Ссылка-приглашение**](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=P__gXIArh5UDBsEq7ttd4WhIYnNh3y1t&authKey=GAsRKEZ%2FwHpzRv19hNJsDnknOc86lYzNIHMPy2Jxt3S3U8f90qestOd760IAj%2F3l&noverify=0&group_code=867785526)
- Присоединяйтесь к нашему серверу Discord: [**Ссылка-приглашение**](https://discord.gg/pnf4ABmDJt)
- Присоединяйтесь к нашей группе в Telegram: [**Ссылка-приглашение**](https://t.me/lightnoble)


## Скриншоты

|                             |
|-----------------------------|
| ![image](assets/light1.png) |
| ![image](assets/light2.png) |
| ![image](assets/light3.png) |

### О EpubLib

Мы разработали отдельный модуль для более эффективного экспорта EPUB. Заинтересованы? Смотрите [**здесь**](https://github.com/dmzz-yyhyy/LightNovelReader/blob/refactoring/epub.md)

## Вклад

Мы приветствуем вклад в LightNovelReader! Вот как вы можете принять участие:

### Начало работы
1. Форкните репозиторий.
2. Клонируйте ваш форк: `git clone https://github.com/your-username/LightNovelReader.git`
3. Создайте новую ветку для ваших изменений: `git checkout -b feature/your-feature-name`
4. Внесите изменения и протестируйте их.
5. Зафиксируйте изменения, следуя приведенным ниже рекомендациям по коммитам.
6. Отправьте в ваш форк: `git push origin feature/your-feature-name`
7. Откройте Pull Request в ветку `refactoring`.

### Рекомендации по коммитам
- Делайте коммиты атомарными и описательными.
- Если ваше изменение влияет на версию, обновите её в `app/build.gradle.kts`.

## Лицензия

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
