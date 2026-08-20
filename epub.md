# PotatoEpubLib

这是一个专门用于生成Epub的模块

## 示例

```kotlin
import io.nightfish.potatoepub.builder.EpubBuilder
import io.nightfish.potatoepub.xml.XmlBuilder.Companion.xml
import java.io.File
import java.time.LocalDateTime

fun main() {
    val rootPath = ClassLoader.getSystemClassLoader().getResource("")!!.toURI()
    EpubBuilder()
        .apply {
            title = "yuk的超级大"
            modifier = LocalDateTime.now()
            cover(File(ClassLoader.getSystemClassLoader().getResource("cover.jpg")?.toURI()!!))
            chapter {
                title("干夜鱼的一百种方法")
                content {
                    title("干夜鱼的一百种方法")
                    text("话说很久以前，有只叫做夜鱼的狐狸，他终日与他人尾交......")
                    image(File(ClassLoader.getSystemClassLoader().getResource("70d0a8f050ac1b4dfffb5665ce052498.jpg")?.toURI()!!))
                }
            }
            chapter {
                title("附录")
                chapter {
                    title("夜鱼的学校生活")
                    content {
                        title("夜鱼的学校生活")
                        text("话说很久以前，有只叫做夜鱼的狐狸，他每日接受着A高附中的折磨")
                    }
                }
                chapter {
                    title("神秘章节")
                    content(
                        xml("html", "http://www.w3.org/1999/xhtml") {
                            "head" {
                                "title" { "神秘章节" }
                            }
                            "body"{
                                "a"("href" to "https://pornhub.com") {
                                    "click for 0721"
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

## 详解

### 基础属性

首先你需要创建一个EpubBuilder

```kotlin
EpubBuilder()
```

然后在内部进行操作
必须被设置的属性有``title`` ``modifier``他们分别是书本的标题和创建时间

```kotlin
EpubBuilder().apply {
    title = "yuk的超级大"
    modifier = LocalDateTime.now()
}
```

同时，你可选的可以指定一个``cover``同时向里面传入一个``File``实体(请确保该File的有效性)

### 添加章节

这之后你可以开始向epub中添加章节了，章节是有层级的，这可以很明显的看出来

```kotlin
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

但值得注意的是，当一个``chapter``中存在了`content`时，就不可以存在``chapter``，反之亦然，``chapter``和
``content``互斥。同时一个``chapter``可以包含多个``chapter``，但仅可以包含一个``content``。每个
``chapter``必须指定``title``。

### 章节内容

``content``的本质是``Document``，你可以选择使用``SimpleContentBuilder``来创建或者手动创建

#### SimpleContentBuilder

你无需手动创建，也不应手动创建该类，而是直接使用chapter下的content提供的环境

```kotlin
chaper {
    ......
    content {
        title("干夜鱼的一百种方法")
        text("话说很久以前，有只叫做夜鱼的狐狸，他终日与他人尾交......")
        image(File(ClassLoader.getSystemClassLoader().getResource("70d0a8f050ac1b4dfffb5665ce052498.jpg")?.toURI()!!))
    }
}
```

你可以添加三种内容，``title``, ``text``, ``image``
其中title限制一个，其余不做限制，类容将会按照你的调用顺序排序，并自动生成为``Document``后传入
``chapter``

#### XmlBuilder

这是用于快速创建xml文件工具，以下为示例

```kotlin
xml("html", "http://www.w3.org/1999/xhtml") {
    "head" {
        "title" { "神秘章节" }
    }
    "body"{
        "a"("href" to "https://pornhub.com") {
            "click for 0721"
        }
        "p" { "Ciallo~(∠・ω< )⌒☆" }
    }
}
```

以上代码会返回一个``Document``对象
