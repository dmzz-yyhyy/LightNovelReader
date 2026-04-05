import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    id("com.mikepenz.aboutlibraries.plugin.android")
}

android {
    namespace = "indi.dmzz_yyhyy.lightnovelreader"
    compileSdk = 36

    defaultConfig {
        multiDexEnabled = true
        applicationId = "indi.dmzz_yyhyy.lightnovelreader"
        minSdk = 24
        targetSdk = 36
        // 版本号为x.y.z则versionCode为x*1000000+y*10000+z*1000+debug版本号(开发需要时迭代, 三位数)
        versionCode = 1_02_00_039
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    @Suppress("UnstableApiUsage")
    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            vcsInfo.include = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isJniDebuggable = true
            vcsInfo.include = false
            versionNameSuffix = "-" + defaultConfig.versionCode.toString()
        }

        register("snapshot") {
            initWith(getByName("release"))
            matchingFallbacks.add("relese")
            applicationIdSuffix = ".snapshot"
            isShrinkResources = true
            isMinifyEnabled = true
            vcsInfo.include = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.US)
            versionNameSuffix = "_SN (${dateFormat.format(Date())})"
        }

        base {
            archivesName = "LightNovelReader-${defaultConfig.versionName}"
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

androidComponents {
    onVariants(selector().withBuildType("snapshot")) { variant ->
        variant.outputs.forEach { output ->
            val outputImpl = output as com.android.build.api.variant.impl.VariantOutputImpl
            val originalFileName = outputImpl.outputFileName.get()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val newFileName = originalFileName.replace(".apk", " (${dateFormat.format(Date())}).apk")
            outputImpl.outputFileName = newFileName
        }
    }
}

kotlin {
    jvmToolchain(21)
}

composeCompiler {
    includeSourceInformation = true
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xwhen-expressions=indy"
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:-processing")
}

dependencies {
    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    // Android lib
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.foundation)
    implementation(libs.core.splashscreen)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    // Compose
    implementation(libs.compose.animation.graphics)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    androidTestImplementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    androidTestImplementation(libs.compose.ui.test.junit4)
    implementation(libs.kotlin.compose.compiler.plugin)
    // Junit
    testImplementation(libs.junit)
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.common)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.hilt.navigation.compose)
    // Navigation
    implementation(libs.navigation.ui.ktx)
    implementation(libs.navigation.compose)
    // coil3
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    // jsoup
    implementation(libs.jsoup)
    // Markdown
    implementation(libs.markdown)
    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    // Splash API
    implementation(libs.core.splashscreen)
    // WorkManager
    implementation(libs.work.runtime.ktx)
    // Potato EPUB
    implementation(project(":epub"))
    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.cbor)
    // Swipe
    implementation(libs.swipe)
    // Chart
    implementation(libs.vico.compose.m3)
    // Potato Auto Proxy
    implementation(project(":proxy"))
    // Telephoto
    implementation(libs.zoomable.image.coil)
    // Shimmer
    implementation(libs.compose.shimmer)
    // About Libraries
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose.m3)
    // LNR API
    implementation(project(":api"))
    implementation(libs.dom4j)
    implementation(libs.kotlin.result)
    implementation(libs.kotlin.result.coroutines)
    // apksig
    implementation(libs.apksig)
    // http
    implementation(libs.cxhttp)
    implementation(libs.okhttp)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.androidx.profileinstaller)
    // RE2J
    implementation(libs.re2j)
    // Matomo
    implementation(libs.matomo.sdk.android)
}

configurations.implementation {
    exclude(group = "com.intellij", module = "annotations")
}

tasks.register("printVersion") {
    doFirst {
        println(android.defaultConfig.versionName)
    }
}

tasks.register("printVersionCode") {
    doFirst {
        println(android.defaultConfig.versionCode)
    }
}