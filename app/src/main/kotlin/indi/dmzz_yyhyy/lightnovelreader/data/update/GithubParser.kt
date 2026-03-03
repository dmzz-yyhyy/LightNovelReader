package indi.dmzz_yyhyy.lightnovelreader.data.update

import android.util.Log
import androidx.compose.ui.util.fastFilter
import indi.dmzz_yyhyy.lightnovelreader.utils.md.HtmlToMdUtil
import kotlinx.coroutines.flow.MutableStateFlow
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

/***
 * Github的更新源获取
 * 我们知道github api
 * 但是为了可以让github加速生效(大多数加速方案不支持github api), 我们被迫选择解析网页
 */
object GithubParser {
    private val versionCodeRegex = Regex("versionCode = (.*)")
    private val versionNameRegex = Regex("versionName = (.*)")
    private val regex = Regex("([0-9]*\\.[0-9]*\\.[0-9]*\\.[0-9]*).*[^.]github.com\\n")
    private const val RAW_HOST = "https://github.com"
    private const val PROXY_HOST = "https://dgithub.xyz"
    private var host = RAW_HOST
    private fun updateHost(): String {
        try {
            Jsoup.connect(host).timeout(1500).get()
            return host
        } catch (_: Exception) { }
        try {
            Jsoup.connect(RAW_HOST).timeout(1500).get()
            return RAW_HOST
        } catch (_: Exception) { }
        try {
            Jsoup.connect(PROXY_HOST).timeout(1500).get()
            return PROXY_HOST
        } catch (_: Exception) {}
        try {
            Jsoup
                .connect("https://gh-proxy.com/raw.githubusercontent.com/frankwuzp/github-host/main/hosts")
                .ignoreContentType(true)
                .get()
                .outputSettings(
                    Document.OutputSettings()
                        .prettyPrint(false)
                        .syntax(Document.OutputSettings.Syntax.xml)
                )
                .toString()
                .let(regex::find)
                ?.groups
                ?.get(1)
                ?.value
                ?.let { return "http://$it" }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return RAW_HOST
    }

    class GithubRelease(
        override val version: Int,
        override val versionName: String,
        override val releaseNotes: String,
        override val downloadUrl: String,
        override val downloadFileProgress: ((File, File) -> Unit)? = null
    ): Release

    private fun progressReleasePage(url: String, updatePhase: MutableStateFlow<String>): Release? {
        Jsoup
            .connect(url)
            .also {
                if (url.startsWith("http://")) it.header("Host", "github.com")
            }
            .get()
            .let { releaseDocument ->
                updatePhase.tryEmit("GitHub步骤: 获取apk下载链接")
                val downloadUrl = releaseDocument
                    .select("include-fragment")
                    .fastFilter { it.attr("src").contains("releases") }
                    .first()
                    .attr("src")
                    .replace("https://github.com", host)
                    .let(Jsoup::connect)
                    .header("Host", "github.com")
                    .get()
                    .select("body > div.Box.Box--condensed.mt-3 > ul > li > div.d-flex.flex-justify-start.col-12.col-lg-9 > a")
                    .map { it.attr("href") }
                    .firstOrNull { it.endsWith("apk") }
                    ?.let { "https://gh-proxy.com/github.com$it" } ?: Log.e("GithubParser", "failed to get downloadUrl").let { return null }
                updatePhase.tryEmit("GitHub步骤: 拉取远程分支版本号")
                val gradle = releaseDocument
                    .select("#repo-content-pjax-container > div > div > div > div.Box-body > div.mb-3.pb-md-4.border-md-bottom > div > div:nth-child(3) > a")
                    .attr("href")
                    .replace("/dmzz-yyhyy/LightNovelReader/tree/", "")
                    .let { "https://gh-proxy.com/raw.githubusercontent.com/dmzz-yyhyy/LightNovelReader/refs/tags/$it/app/build.gradle.kts" }
                    .let(Jsoup::connect)
                    .ignoreContentType(true)
                    .get()
                    .outputSettings(
                        Document.OutputSettings()
                            .prettyPrint(false)
                            .syntax(Document.OutputSettings.Syntax.xml)
                    )
                    .toString()
                val versionCode = versionCodeRegex.find(gradle)?.groups?.get(1)?.value?.replace("_", "")?.toInt() ?: Log.e("GithubParser", "failed to get versionCode").also { return null }
                val versionName = versionNameRegex.find(gradle)?.groups?.get(1)?.value ?: Log.e("GithubParser", "failed to get versionName").also { return null }
                updatePhase.tryEmit("GitHub步骤: 解析更新日志")
                val releaseNotes = releaseDocument
                    .select("#repo-content-pjax-container > div > div > div > div.Box-body > div.markdown-body.my-3")
                    .toString()
                    .let(HtmlToMdUtil::convertHtml)
                return GithubRelease(
                    versionCode,
                    versionName.toString(),
                    releaseNotes,
                    downloadUrl
                )
            }
    }

    object ReleaseParser: UpdateParser {
        private const val URL = "/dmzz-yyhyy/LightNovelReader"
        override fun parser(updatePhase: MutableStateFlow<String>): Release? {
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true")
            host = updateHost()
            return Jsoup
                .connect(host+ URL)
                .also {
                    if (host.startsWith("http://")) it.header("Host", "github.com")
                }
                .get()
                .select("#repo-content-pjax-container > div > div > div > div.Layout-sidebar > div > div:nth-child(2) > div > a")
                .attr("href")
                .let { host+it }
                .also { updatePhase.tryEmit("GitHub步骤: 获取最新Release") }
                .let { progressReleasePage(it, updatePhase) }
        }
    }
    object DevelopmentParser: UpdateParser {
        private const val URL = "/dmzz-yyhyy/LightNovelReader/releases"
        override fun parser(updatePhase: MutableStateFlow<String>): Release? {
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true")
            host = updateHost()
            return Jsoup
                .connect(host+ URL)
                .also {
                    if (host.startsWith("http://")) it.header("Host", "github.com")
                }
                .get()
                .select("#repo-content-pjax-container > div > div:nth-child(3) > section:nth-child(1) > div > div.col-md-9 > div > div.Box-body > div.d-flex.flex-md-row.flex-column > div.d-flex.flex-row.flex-1.mb-3.wb-break-word > div.flex-1 > span.f1.text-bold.d-inline.mr-3 > a")
                .attr("href")
                .also { updatePhase.tryEmit("GitHub步骤: 获取最新Release") }
                .let { host+it }
                .let { progressReleasePage(it, updatePhase) }
        }
    }
    object CIParser: UpdateParser {
        private const val URL = "/dmzz-yyhyy/LightNovelReader/actions/workflows/marge.yml"
        private val prIdRegex = Regex("Merge pull request #([0-9]*)")
        override fun parser(updatePhase: MutableStateFlow<String>): Release? {
            System.setProperty("sun.net.http.allowRestrictedHeaders", "true")
            host = updateHost()
            updatePhase.tryEmit("GitHub步骤: 获取最新Release")
            val downloadUrl: String?
            updatePhase.tryEmit("GitHub步骤: 拉取远程分支版本号")
            val gradle = Jsoup
                .connect("https://gh-proxy.com/raw.githubusercontent.com/dmzz-yyhyy/LightNovelReader/refs/heads/refactoring/app/build.gradle.kts")
                .ignoreContentType(true)
                .get()
                .outputSettings(
                    Document.OutputSettings()
                        .prettyPrint(false)
                        .syntax(Document.OutputSettings.Syntax.xml)
                )
                .toString()
            val versionCode = versionCodeRegex.find(gradle)?.groups?.get(1)?.value?.replace("_", "")?.toInt() ?: Log.e("GithubParser", "failed to get versionCode").also { return null }
            val versionName = versionNameRegex.find(gradle)?.groups?.get(1)?.value?.replace("\"", "") ?: Log.e("GithubParser", "failed to get versionName").also { return null }
            val connection = Jsoup.connect(host + URL)
            if (host.startsWith("http://")) {
                connection.header("Host", "github.com")
            }
            val document = connection.get()
            updatePhase.tryEmit("Github步骤: 获取apk下载链接")
            val apkLinkElement = document.select(
                "div[id^=check_suite_]:contains(ReleaseApkBuild) > div > div.d-table-cell.v-align-top.col-11.col-md-6.position-relative > a"
            ).first()
            val actionUrl = "https://nightly.link" + apkLinkElement?.attr("href")
            val fileDocument = Jsoup.connect(actionUrl).get()
            val apkDownloadHref = fileDocument
                .select("body > article > table > tbody > tr > td > a")
                .attr("href")
            downloadUrl = apkDownloadHref
            val downloadFileProgress: ((File, File) -> Unit) = { zipFile, targetApk ->
                try {

                    ZipFile(zipFile).use { zip ->
                        val apkEntry = zip.entries().asSequence()
                            .filterNot { it.isDirectory }
                            .find { entry ->
                                entry.name.endsWith(".apk") &&
                                        "release" in entry.name
                            }

                            ?: throw IOException("failed to extract apk file from archive [${zipFile.name}]")

                        targetApk.parentFile?.mkdirs()
                        if (targetApk.exists()) targetApk.delete()

                        zip.getInputStream(apkEntry).use { input ->
                            targetApk.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Unzip", "failed to extract: ${e.message}")
                    targetApk.delete()
                    throw e
                }
            }
            updatePhase.tryEmit("GitHub步骤: 获取更新日志")

            val spanText = document.select("span").text()
            val prId = prIdRegex.find(spanText)?.groups?.get(1)?.value

            val prUrl = prId?.let { "$host/dmzz-yyhyy/LightNovelReader/pull/$it" }
            val prConnection = prUrl?.let { Jsoup.connect(it) }
            if (host.startsWith("http://")) {
                prConnection?.header("Host", "github.com")
            }

            val taskListElement = prConnection?.get()?.select(
                "#discussion_bucket > div > div.Layout-main > div > div.js-discussion.ml-0.pl-0.ml-md-6.pl-md-3 > div.TimelineItem.TimelineItem--condensed.pt-0.js-comment-container.js-socket-channel.js-updatable-content.js-command-palette-pull-body > div.timeline-comment-group.js-minimizable-comment-group.js-targetable-element.TimelineItem-body.my-0 > div > div:nth-child(2) > div > task-lists > div"
            )?.toString()

            val releaseNotes = taskListElement
                ?.let(HtmlToMdUtil::convertHtml)
                ?.let { "**注意! 这是一个由 GitHub Actions 构建出来的版本, 此版本未经过严格测试**\n\n$it" }

            updatePhase.tryEmit("GitHub步骤: 比对版本号")
            val lastReleaseRelease = ReleaseParser.parser(MutableStateFlow(""))
            return if (lastReleaseRelease == null || lastReleaseRelease.version < versionCode)
                GithubRelease(
                    versionCode,
                    versionName.toString() ,
                    releaseNotes!!,
                    downloadUrl,
                    downloadFileProgress
                )
            else lastReleaseRelease
        }
    }
}