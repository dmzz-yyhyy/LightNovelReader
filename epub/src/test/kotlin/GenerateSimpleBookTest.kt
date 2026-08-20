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
                    image(
                        File(
                            ClassLoader.getSystemClassLoader()
                                .getResource("70d0a8f050ac1b4dfffb5665ce052498.jpg")?.toURI()!!
                        )
                    )
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
                            "body" {
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