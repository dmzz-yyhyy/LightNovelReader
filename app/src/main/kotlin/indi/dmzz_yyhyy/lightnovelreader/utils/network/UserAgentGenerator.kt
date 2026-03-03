package indi.dmzz_yyhyy.lightnovelreader.utils.network

import kotlin.random.Random

object UserAgentGenerator {

    private val androidVersions = listOf(
        "7.0", "7.1.1", "8.0.0", "8.1.0",
        "9", "10", "11", "12", "13", "14"
    )

    private val deviceModels = listOf(
        "Pixel 4", "Pixel 5", "Pixel 6", "Pixel 7",
        "SM-G991B", "SM-G996B", "SM-S908B",
        "M2102J20SG", "2201123G"
    )

    private val chromeMajorVersions = (100..140).toList()

    fun generate(): String {
        val androidVersion = androidVersions.random()
        val model = deviceModels.random()
        val buildId = randomBuildId()

        val chromeMajor = chromeMajorVersions.random()
        val chromeMinor = Random.nextInt(0, 4000)
        val chromeBuild = Random.nextInt(0, 200)
        val chromePatch = Random.nextInt(0, 150)

        return "Mozilla/5.0 (Linux; Android $androidVersion; $model Build/$buildId) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/$chromeMajor.0.$chromeMinor.$chromeBuild Mobile Safari/537.$chromePatch"
    }

    private fun randomBuildId(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        val length = listOf(6, 7, 8, 10).random()
        return (1..length).map { chars.random() }
            .joinToString("")
    }

}

fun randomUAHeadersJsoup(): Map<String, String> {
    return mapOf(
        "User-Agent" to UserAgentGenerator.generate(),
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Accept-Language" to "en-US,en;q=0.9",
        "Upgrade-Insecure-Requests" to "1"
    )
}
