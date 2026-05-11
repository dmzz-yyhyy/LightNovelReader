import java.net.URI
import java.util.Properties

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

plugins {
    kotlin("jvm")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.symbol.processing.api)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "io.nightfish.lightnovelreader"
            artifactId = "compiler"
            version = "0.4-SNAPSHOT"
        }
    }

    repositories {
        maven {
            name = "reposilite"
            url = URI("https://maven.nariko.org/release")
            credentials {
                username = localProperties["maven.username"]?.toString()
                    ?: System.getenv("MAVEN_USERNAME") ?: ""
                password = localProperties["maven.password"]?.toString()
                    ?: System.getenv("MAVEN_PASSWORD") ?: ""
            }
        }
    }
}
