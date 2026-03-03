import java.net.URI

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
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

android {
    namespace = "io.nightfish.lightnovelreader.api"
    defaultConfig {
        multiDexEnabled = true
        minSdk = 24
    }
    compileSdk = 36

    buildFeatures {
        buildConfig = false
        compose = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }

    buildTypes {
        register("snapshot") {
            initWith(getByName("release"))
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "io.nightfish.lightnovelreader"
                artifactId = "api"
                version = "0.2-SNAPSHOT"
            }
        }

        repositories {
            maven {
                name = "reposilite"
                url = URI("https://maven.curiousers.org/release")
                credentials {
                    username = System.getenv("REPO_USER")
                    password = System.getenv("REPO_PASS")
                }
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.foundation)
    implementation(libs.compose.ui.graphics)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.runtime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(platform(libs.compose.bom))
    implementation(libs.navigation.compose)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.dom4j)
}
