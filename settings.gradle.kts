@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        exclusiveContent {
            forRepository {
                maven {
                    setUrl("https://maven.aliyun.com/repository/public")
                }
            }
            filter {
                includeGroup("com.github.promeg")
            }
        }
        maven { setUrl("https://jitpack.io") }
    }
}

rootProject.name = "LightNovelReaderRefactoring"
include(":app")
include(":epub")
include(":proxy")
include(":api")
include(":plugin:js")
include(":compiler")
