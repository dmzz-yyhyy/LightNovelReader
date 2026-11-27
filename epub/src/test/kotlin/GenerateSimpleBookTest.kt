import io.nightfish.potatoepub.builder.EpubBuilder
import io.nightfish.potatoepub.xml.XmlBuilder.Companion.xml
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GenerateSimpleBookTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun savesBookWithMetadataNestedChaptersAndImages() {
        val coverBytes = resourceBytes("cover.jpg")
        val imageBytes = resourceBytes("70d0a8f050ac1b4dfffb5665ce052498.jpg")
        val cover = temporaryDirectory.resolve("cover.jpg").toFile().apply { writeBytes(coverBytes) }
        val image = temporaryDirectory.resolve("illustration.jpg").toFile().apply { writeBytes(imageBytes) }
        val bookTitle = "测试书籍 & EPUB"
        val chapterText = "章节正文 <one> & two"
        val builder = EpubBuilder().apply {
            id = "test-book"
            title = bookTitle
            creator = "Test author"
            language = Locale.CHINESE
            modifier = LocalDateTime.of(2026, 1, 2, 3, 4, 5)
            cover(cover)
            chapter {
                title("第一章")
                content {
                    title("第一章")
                    text(chapterText)
                    image(image, id = "illustration", src = "image/illustration.jpg")
                }
            }
            chapter {
                title("附录")
                chapter {
                    title("第二章")
                    content {
                        title("第二章")
                        text("Nested chapter text")
                    }
                }
                chapter {
                    title("第三章")
                    content(xml("html", "http://www.w3.org/1999/xhtml") {
                        "head" { "title" { "第三章" } }
                        "body" { "p" { "Custom chapter text" } }
                    })
                }
            }
        }
        val target = temporaryDirectory.resolve("output/book.epub").toFile()

        builder.build().save(target)

        assertTrue(target.isFile)
        ZipFile(target).use { zip ->
            val entries = zip.entries().asSequence().toList()
            assertEquals("mimetype", entries.first().name)
            assertEquals(ZipEntry.STORED, entries.first().method)
            assertEquals("application/epub+zip", zip.readText("mimetype"))
            assertEquals(entries.size, entries.map { it.name }.toSet().size)

            val container = zip.readXml("META-INF/container.xml")
            assertEquals(
                "EPUB/content.opf",
                container.rootElement.element("rootfiles").element("rootfile").attributeValue("full-path")
            )
            val opf = zip.readXml("EPUB/content.opf").rootElement
            val metadata = opf.element("metadata")
            assertEquals("test-book", metadata.elementText("identifier"))
            assertEquals(bookTitle, metadata.elementText("title"))
            assertEquals("Test author", metadata.elementText("creator"))
            assertEquals("zh", metadata.elementText("language"))
            assertEquals(
                "2026-01-02T03:04:05Z",
                metadata.elements("meta").single { it.attributeValue("property") == "dcterms:modified" }.text
            )

            val manifest = opf.element("manifest").elements("item")
            manifest.forEach { item ->
                assertNotNull(zip.getEntry("EPUB/${item.attributeValue("href")}"), "Missing manifest resource: $item")
            }
            assertEquals(
                "cover-image",
                manifest.single { it.attributeValue("id") == "cover" }.attributeValue("properties")
            )
            assertEquals(
                "image/jpeg",
                manifest.single { it.attributeValue("id") == "illustration" }.attributeValue("media-type")
            )
            assertContentEquals(coverBytes, zip.readBytes("EPUB/cover.jpg"))
            assertContentEquals(imageBytes, zip.readBytes("EPUB/image/illustration.jpg"))

            val manifestById = manifest.associateBy { it.attributeValue("id") }
            val chapterPaths = opf.element("spine").elements("itemref").map { item ->
                assertNotNull(manifestById[item.attributeValue("idref")]).attributeValue("href")
            }
            assertEquals(3, chapterPaths.size)
            val chapters = chapterPaths.map { zip.readXml("EPUB/$it").rootElement }
            assertEquals(
                listOf("第一章", "第二章", "第三章"),
                chapters.map { it.element("head").elementText("title") }
            )
            assertEquals(chapterText, chapters.first().element("body").element("div").text.trim())
            assertEquals(
                "image/illustration.jpg",
                chapters.first().element("body").element("div").element("div").element("img").attributeValue("src")
            )
            assertEquals("Nested chapter text", chapters[1].element("body").element("div").text.trim())
            assertEquals("Custom chapter text", chapters[2].element("body").elementText("p"))

            val navigation = zip.readXml("EPUB/nav.xhtml").rootElement.element("body").element("nav").element("ol")
            val navigationItems = navigation.elements("li")
            assertEquals("附录", navigationItems[1].elementText("span"))
            val links = navigationItems[0].elements("a") +
                navigationItems[1].element("ol").elements("li").map { it.element("a") }
            assertEquals(listOf("第一章", "第二章", "第三章"), links.map { it.text })
            assertEquals(chapterPaths, links.map { it.attributeValue("href") })

            val navPoints = zip.readXml("EPUB/toc.ncx").rootElement.element("navMap").elements("navPoint")
            assertEquals("附录", navPoints[1].element("navLabel").elementText("text"))
            assertEquals(chapterPaths[1], navPoints[1].element("content").attributeValue("src"))
            val contentPoints = listOf(navPoints[0]) + navPoints[1].elements("navPoint")
            assertEquals(chapterPaths, contentPoints.map { it.element("content").attributeValue("src") })
        }
    }

    private fun resourceBytes(name: String): ByteArray =
        assertNotNull(javaClass.getResourceAsStream("/$name"), "Missing test resource: $name").use { it.readBytes() }

    private fun ZipFile.readBytes(path: String): ByteArray =
        getInputStream(assertNotNull(getEntry(path), "Missing ZIP entry: $path")).use { it.readBytes() }

    private fun ZipFile.readText(path: String): String = readBytes(path).toString(Charsets.UTF_8)

    private fun ZipFile.readXml(path: String): Document = DocumentHelper.parseText(readText(path))
}
