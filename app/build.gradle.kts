import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.dagger.hilt)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
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
        versionCode = 1_01_02_001
        versionName = "1.1.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            setProperty("archivesBaseName", "LightNovelReader-${defaultConfig.versionName}")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isJniDebuggable = true
            setProperty("archivesBaseName", "LightNovelReader-${defaultConfig.versionCode}")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    composeCompiler {
        includeSourceInformation = true
    }
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

dependencies {
    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    // Android lib
    implementation(libs.androidx.core.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    // Compose
    implementation(libs.activity.compose)
    implementation(libs.compose.animation.graphics)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    androidTestImplementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
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
    // coil
    implementation(libs.coil.compose)
    // jsoup
    implementation(libs.jsoup)
    // Gson
    implementation(libs.gson)
    // Markdown
    implementation(libs.markdown)
    // Ketch
    implementation(libs.ketch)
    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    // Splash API
    implementation(libs.core.splashscreen)
    // WorkManager
    implementation(libs.work.runtime.ktx)
    // Potato EPUB
    implementation(project(":epub"))
    implementation(libs.serialization.json)
    // Swipe
    implementation(libs.swipe)
    // Chart
    implementation(libs.vico.compose.m3)
    // Potato Auto Proxy
    implementation(project(":proxy"))
    // Telephoto
    implementation(libs.zoomable.image.coil)
}

configurations.implementation{
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