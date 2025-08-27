[简体中文](README.md) | [繁體中文](README_TW.md) | [English](README_US.md) | **Русский**

<div align="center">
    <h1>LightNovelReader</h1>
    <a><img alt="Android" src="https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white&style=for-the-badge"/></a>
    <a><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-0095D5.svg?logo=kotlin&logoColor=white&style=for-the-badge"/></a>
    <a><img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white&style=for-the-badge"></a>
    <a href="http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=P__gXIArh5UDBsEq7ttd4WhIYnNh3y1t&authKey=GAsRKEZ%2FwHpzRv19hNJsDnknOc86lYzNIHMPy2Jxt3S3U8f90qestOd760IAj%2F3l&noverify=0&group_code=867785526"><img alt="QQ Group" src="https://img.shields.io/badge/QQ讨论群-867785526-brightgreen.svg?logoColor=white&style=for-the-badge"></a>
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
- Используйте формат conventional commit: `<type>(<scope>): <description>`
  - Типы: `feat` (новая функция), `fix` (исправление ошибки), `docs` (документация), `style` (форматирование), `refactor` (реструктуризация кода), `test` (тестирование), `chore` (обслуживание)
  - Пример: `feat: добавить поддержку темного режима`
- Делайте коммиты атомарными и описательными.
- Если ваше изменение влияет на версию, обновите её в `app/build.gradle.kts`.

### Управление версиями
Версии управляются в `app/build.gradle.kts`:
- `versionNameStr`: Публичная версия (например, "1.3.1"). Следуйте семантическому версионированию (major.minor.patch).
- `debugNumber`: Увеличивайте для сборок разработки (0 для релизов).
- `versionCode`: Автоматически рассчитывается как major*1000000 + minor*10000 + patch*1000 + debugNumber.

Для релизов обновите `versionNameStr` и сбросьте `debugNumber` до 0.

## Лицензия

```
Авторские права (C) 2024 NightFish <hk198580666@outlook.com>
Авторские права (C) 2024 yukonisen <yukonisen@curiousers.org>

Данная программа является свободным программным обеспечением: вы можете распространять и/или изменять её
в соответствии с условиями лицензии GNU General Public License, опубликованной
Фондом свободного программного обеспечения, версии 3 лицензии или (по вашему выбору) любой более поздней версии.

Программа распространяется в надежде, что она будет полезной,
но БЕЗ ЛЮБЫХ ГАРАНТИЙ, в том числе без явной или подразумеваемой гарантии
КОММЕРЧЕСКОЙ ПРИГОДНОСТИ ИЛИ ПРИГОДНОСТИ ДЛЯ КОНКРЕТНОЙ ЦЕЛИ. Подробнее см. в лицензии GNU General Public License.

Вы должны были получить копию GNU General Public License
вместе с этой программой. Если нет, см. <http://www.gnu.org/licenses/>.
```
