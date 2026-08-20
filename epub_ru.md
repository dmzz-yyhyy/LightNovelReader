# PotatoEpubLib

Это модуль, специально созданный для генерации Epub.

## Пример

```kotlin
import io.nightfish.potatoepub.builder.EpubBuilder
import io.nightfish.potatoepub.xml.XmlBuilder.Companion.xml
import java.io.File
import java.time.LocalDateTime

fun main() {
    val rootPath = ClassLoader.getSystemClassLoader().getResource("")!!.toURI()
    EpubBuilder()
        .apply {
            title = "Супербольшая книга Yuk"
            modifier = LocalDateTime.now()
            cover(File(ClassLoader.getSystemClassLoader().getResource("cover.jpg")?.toURI()!!))
            chapter {
                title("Сто способов обработать ночную рыбу")
                content {
                    title("Сто способов обработать ночную рыбу")
                    text("Давным-давно была лиса по имени Ночная рыба, которая целыми днями занималась любовными делами с другими...")
                    image(File(ClassLoader.getSystemClassLoader().getResource("70d0a8f050ac1b4dfffb5665ce052498.jpg")?.toURI()!!))
                }
            }
            chapter {
                title("Приложение")
                chapter {
                    title("Школьная жизнь Ночной рыбы")
                    content {
                        title("Школьная жизнь Ночной рыбы")
                        text("Давным-давно была лиса по имени Ночная рыба, которая ежедневно терпела мучения в старшей школе А")
                    }
                }
                chapter {
                    title("Таинственная глава")
                    content(
                        xml("html", "http://www.w3.org/1999/xhtml") {
                            "head" {
                                "title" { "Таинственная глава" }
                            }
                            "body"{
                                "a"("href" to "https://pornhub.com") {
                                    "Нажмите для 0721"
                                }
                                "p" { "Ciallo~(∠・ω< )⌒☆" }
                            }
                        }.addDocType("html", "", "")
                    )
                }
            }
        }
        .build()
        .save(File(rootPath.resolve("generate/test.epub")))
}
```

## Подробный разбор

### Основные свойства

Сначала нужно создать объект `EpubBuilder`:

```
EpubBuilder()
```

Затем выполнять действия внутри блока `.apply`. Обязательные для установки свойства — `title` и
`modifier`, это название книги и дата создания:

```
EpubBuilder().apply {
    title = "Супербольшая книга Yuk"
    modifier = LocalDateTime.now()
}
```

Опционально можно указать обложку (`cover`), передав туда объект типа `File` (проверьте корректность
файла).

### Добавление глав

После этого можно начать добавлять главы. Главы организованы иерархически, что видно из структуры:

```
chapter {
    title("xxx")
    chapter {
        title("xxxx")
        content(...)
    }
    chapter {
        title("xxxx")
        content(...)
    }
}
chapter {
    title("xxxx")
    content(...)
}
```

Важно: если у `chapter` есть `content`, то он не может содержать другие `chapter` и наоборот —
`chapter` и `content` взаимно исключают друг друга. Глава может содержать несколько вложенных глав,
но только один `content`. Каждая глава должна иметь указанный `title`.

### Содержимое главы

`content`по сути является объектом `Document`. Его можно создать используя либо
`SimpleContentBuilder`, либо вручную.

#### SimpleContentBuilder

Не нужно создавать этот класс вручную, лучше использовать среду, предоставляемую внутри `chapter`
для `content`:

```
chapter {
    ...
    content {
        title("Сто способов обработать ночную рыбу")
        text("Давным-давно была лиса по имени Ночная рыба, которая целыми днями занималась любовными делами с другими...")
        image(File(ClassLoader.getSystemClassLoader().getResource("70d0a8f050ac1b4dfffb5665ce052498.jpg")?.toURI()!!))
    }
}
```

Вы можете добавлять три типа содержимого: `title`, `text` и `image`. Заголовок допускается только
один, остальные можно добавлять несколько. Контент будет сгенерирован в порядке вызова методов и
автоматически конвертирован в объект `Document` для передачи в `chapter`.

#### XmlBuilder

Это утилита для быстрого создания XML-файлов. Пример:

```
xml("html", "http://www.w3.org/1999/xhtml") {
    "head" {
        "title" { "Таинственная глава" }
    }
    "body"{
        "a"("href" to "https://pornhub.com") {
            "Нажмите для 0721"
        }
        "p" { "Ciallo~(∠・ω< )⌒☆" }
    }
}
```

Этот код возвращает объект `Document`.
